package com.github.whyrising.y.concretions.map

import com.github.whyrising.y.concretions.list.ASeq
import com.github.whyrising.y.concretions.list.PersistentList
import com.github.whyrising.y.map.APersistentMap
import com.github.whyrising.y.map.IMapEntry
import com.github.whyrising.y.map.IPersistentMap
import com.github.whyrising.y.map.MapIterable
import com.github.whyrising.y.mutable.collection.IMutableCollection
import com.github.whyrising.y.mutable.map.ATransientMap
import com.github.whyrising.y.mutable.map.TransientMap
import com.github.whyrising.y.seq.IPersistentCollection
import com.github.whyrising.y.seq.ISeq
import com.github.whyrising.y.util.equiv
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.atomicfu.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.collections.Map.Entry
import kotlin.math.max

const val HASHTABLE_THRESHOLD = 16

internal class PersistentArrayMapSerializer<K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
) : KSerializer<PersistentArrayMap<K, V>> {
    internal val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): PersistentArrayMap<K, V> =
        mapSerializer.deserialize(decoder).toPArrayMap()

    override fun serialize(encoder: Encoder, value: PersistentArrayMap<K, V>) {
        return mapSerializer.serialize(encoder, value)
    }
}

@Serializable(PersistentArrayMapSerializer::class)
class PersistentArrayMap<out K, out V>(
    internal val array: Array<Pair<@UnsafeVariance K, @UnsafeVariance V>>
) : APersistentMap<K, V>(), MapIterable<K, V>, IMutableCollection<Any?> {

    @Suppress("UNCHECKED_CAST")
    private fun createArrayMap(newPairs: Array<out Pair<K, V>?>) =
        PersistentArrayMap(newPairs as Array<Pair<K, V>>)

    private fun indexOf(key: @UnsafeVariance K): Int {
        for (i in array.indices)
            if (equiv(key, array[i].first)) return i

        return -1
    }

    private fun keyIsAlreadyAvailable(index: Int): Boolean = index >= 0

    @ExperimentalStdlibApi
    override fun assoc(
        key: @UnsafeVariance K,
        value: @UnsafeVariance V
    ): IPersistentMap<K, V> {
        val index: Int = indexOf(key)
        val newPairs: Array<out Pair<K, V>?>

        when {
            keyIsAlreadyAvailable(index) -> {
                if (array[index].second == value) return this

                newPairs = array.copyOf()
                newPairs[index] = Pair(key, value)
            }
            else -> {
                if (array.size >= HASHTABLE_THRESHOLD)
                    return PersistentHashMap(*array).assoc(key, value)

                newPairs = arrayOfNulls(array.size + 1)

                if (array.isNotEmpty())
                    array.copyInto(newPairs, 0, 0, array.size)

                newPairs[newPairs.size - 1] = Pair(key, value)
            }
        }

        return createArrayMap(newPairs)
    }

    @ExperimentalStdlibApi
    override fun assocNew(
        key: @UnsafeVariance K,
        value: @UnsafeVariance V
    ): IPersistentMap<K, V> {
        val index: Int = indexOf(key)
        val newPairs: Array<out Pair<K, V>?>

        if (keyIsAlreadyAvailable(index))
            throw RuntimeException("The key $key is already present.")

        if (array.size >= HASHTABLE_THRESHOLD)
            return PersistentHashMap(*array).assocNew(key, value)

        newPairs = arrayOfNulls(array.size + 1)

        if (array.isNotEmpty()) array.copyInto(newPairs, 0, 0, array.size)

        newPairs[newPairs.size - 1] = Pair(key, value)

        return createArrayMap(newPairs)
    }

    override fun dissoc(key: @UnsafeVariance K): IPersistentMap<K, V> =
        indexOf(key).let { index ->
            when {
                keyIsAlreadyAvailable(index) -> {
                    val size = array.size - 1

                    if (size == 0) return EmptyArrayMap

                    val newPairs: Array<Pair<K, V>?> = arrayOfNulls(size)
                    array.copyInto(newPairs, 0, 0, index)
                    array.copyInto(newPairs, index, index + 1, array.size)

                    return createArrayMap(newPairs)
                }
                else -> return this
            }
        }

    override fun containsKey(key: @UnsafeVariance K): Boolean =
        keyIsAlreadyAvailable(indexOf(key))

    override fun entryAt(
        key: @UnsafeVariance K
    ): IMapEntry<K, V>? = indexOf(key).let { index ->
        when {
            keyIsAlreadyAvailable(index) -> {
                val (first, second) = array[index]
                MapEntry(first, second)
            }
            else -> null
        }
    }

    override fun valAt(
        key: @UnsafeVariance K,
        default: @UnsafeVariance V?
    ): V? = indexOf(key).let { index ->
        when {
            keyIsAlreadyAvailable(index) -> array[index].second
            else -> default
        }
    }

    override fun valAt(key: @UnsafeVariance K): V? = valAt(key, null)

    override fun seq(): ISeq<MapEntry<K, V>> = when (count) {
        0 -> PersistentList.Empty
        else -> Seq(array, 0)
    }

    override val count: Int = array.size

    override fun empty(): IPersistentCollection<Any?> = EmptyArrayMap

    override fun iterator(): Iterator<Entry<K, V>> = Iter(array, makeMapEntry)

    override fun keyIterator(): Iterator<K> = Iter(array, makeKey)

    override fun valIterator(): Iterator<V> = Iter(array, makeValue)

    override fun asTransient(): TransientMap<K, V> = TransientArrayMap(array)

    internal class Iter<K, V, R>(
        private val array: Array<Pair<@UnsafeVariance K, @UnsafeVariance V>>,
        val f: (Pair<K, V>) -> R
    ) : Iterator<R> {

        var index = 0

        override fun hasNext(): Boolean = index < array.size

        override fun next(): R = when {
            index >= array.size -> throw NoSuchElementException()
            else -> {
                val cached = index
                index++
                f(array[cached])
            }
        }
    }

    internal class Seq<out K, out V>(
        private val array: Array<Pair<@UnsafeVariance K, @UnsafeVariance V>>,
        val index: Int
    ) : ASeq<MapEntry<K, V>>() {

        override val count: Int = array.size - index

        override
        fun first(): MapEntry<K, V> = array[index].let { (first, second) ->
            MapEntry(first, second)
        }

        override fun rest(): ISeq<MapEntry<K, V>> = (index + 1).let { i ->
            when {
                i < array.size -> Seq(array, i)
                else -> PersistentList.Empty
            }
        }
    }

    internal class TransientArrayMap<out K, out V> private constructor(
        internal val array: Array<Pair<@UnsafeVariance K, @UnsafeVariance V>?>,
        edit: Any?,
        length: Int
    ) : ATransientMap<K, V>() {
        private val _edit: AtomicRef<Any?> = atomic(edit)
        private val _length = atomic(length)

        constructor(array: Array<Pair<K, V>>) : this(
            array.copyOf(max(HASHTABLE_THRESHOLD, array.size)),
            Any(),
            array.size
        )

        val edit by _edit
        val length by _length

        override val doCount: Int
            by _length

        override fun ensureEditable() {
            if (_edit.value == null)
                throw IllegalStateException(
                    "Transient used after persistent() call."
                )
        }

        private fun indexOf(key: @UnsafeVariance K): Int {
            for (i in 0 until length)
                if (equiv(key, array[i]?.first)) return i

            return -1
        }

        private val lock = reentrantLock()

        @Suppress("UNCHECKED_CAST")
        override fun doAssoc(
            key: @UnsafeVariance K,
            value: @UnsafeVariance V
        ): TransientMap<K, V> {
            lock.withLock {
                ensureEditable()

                val index = indexOf(key)
                if (index >= 0) {
                    if (array[index]!!.second != value)
                        array[index] = Pair(key, value)
                } else if (_length.value >= array.size) {
                    return PersistentHashMap(*(array as Array<Pair<K, V>>))
                        .asTransient().assoc(key, value)
                } else array[_length.getAndIncrement()] =
                    Pair(key, value)
                return this
            }
        }

        override fun doDissoc(key: @UnsafeVariance K): TransientMap<K, V> {
            lock.withLock {
                val index: Int = indexOf(key)

                if (index >= 0) {
                    _length.update { currentLength: Int ->
                        array[index] = when {
                            currentLength > 1 -> array[currentLength - 1]
                            else -> null
                        }
                        currentLength - 1
                    }
                }
                return this
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun doPersistent(): IPersistentMap<K, V> {
            ensureEditable()

            _edit.value = null
            val ar = arrayOfNulls<Pair<K, V>>(_length.value)
            array.copyInto(ar, 0, 0, ar.size)

            return PersistentArrayMap(ar as Array<Pair<K, V>>)
        }

        override fun doValAt(
            key: @UnsafeVariance K,
            default: @UnsafeVariance V?
        ): V? = indexOf(key).let { index ->
            when {
                index >= 0 -> array[index]!!.second
                else -> default
            }
        }
    }

    companion object {
        internal val EmptyArrayMap =
            PersistentArrayMap<Nothing, Nothing>(emptyArray())

        operator fun <K, V> invoke(): PersistentArrayMap<K, V> = EmptyArrayMap

        private fun <K> areKeysEqual(key1: K, key2: K): Boolean = when (key1) {
            key2 -> true
            else -> equiv(key1, key2)
        }

        @Suppress("UNCHECKED_CAST")
        operator
        fun <K, V> invoke(vararg pairs: Pair<K, V>): PersistentArrayMap<K, V> =
            when {
                pairs.isEmpty() -> EmptyArrayMap
                else -> {
                    for (i in pairs.indices)
                        for (j in i + 1 until pairs.size)
                            if (areKeysEqual(pairs[i].first, pairs[j].first))
                                throw IllegalArgumentException(
                                    "Duplicate key: ${pairs[i].first}"
                                )

                    PersistentArrayMap(pairs as Array<Pair<K, V>>)
                }
            }

        fun <K, V> create(map: Map<K, V>): PersistentArrayMap<K, V> {
            var ret: TransientMap<K, V> = EmptyArrayMap.asTransient()

            for (entry in map.entries) ret = ret.assoc(entry.key, entry.value)

            return ret.persistent() as PersistentArrayMap<K, V>
        }
    }
}

fun <K, V> m(vararg pairs: Pair<K, V>): IPersistentMap<K, V> =
    PersistentArrayMap(*pairs)

fun <K, V> Map<K, V>.toPArrayMap(): PersistentArrayMap<K, V> =
    PersistentArrayMap.create(this)
