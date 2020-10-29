package com.github.whyrising.y

import kotlin.collections.Map.Entry

abstract class APersistentMap<out K, out V> :
    IPersistentMap<K, V>,
    Map<@UnsafeVariance K, V>,
    Iterable<Entry<K, V>>,
    MapEquivalence {

    var hashCode = 0
        private set

    @Suppress("UNCHECKED_CAST")
    override fun toString(): String {
        var seq = seq() as ISeq<MapEntry<K, V>>
        var s = "{"

        while (seq != emptySeq<MapEntry<K, V>>()) {
            val entry = seq.first()
            s += "${entry.key} ${entry.value}"
            seq = seq.rest()

            if (seq != emptySeq<MapEntry<K, V>>()) s += ", "
        }

        s += '}'

        return s
    }

    @Suppress("UNCHECKED_CAST")
    override fun hashCode(): Int {
        var cashed = hashCode

        if (cashed == 0) {
            var seq = seq() as ISeq<MapEntry<K, V>>

            while (seq != emptySeq<MapEntry<K, V>>()) {
                val entry = seq.first()

                cashed += entry.key.hashCode() xor entry.value.hashCode()

                seq = seq.rest()
            }

            hashCode = cashed
        }

        return cashed
    }

    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?): Boolean {
        when {
            other !is Map<*, *> -> return false
            count != other.size -> return false
            else -> {
                var seq = seq()
                val map = other as Map<K, V>

                for (i in 0 until count) {
                    val entry = seq.first() as Entry<K, V>
                    val key = entry.key
                    val keyFound = map.containsKey(key)

                    if (!keyFound || entry.value != map.getValue(key))
                        return false

                    seq = seq.rest()
                }

                return true
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun conj(e: Any?): IPersistentCollection<Any?> = when (e) {
        null -> this
        is Entry<*, *> -> assoc(e.key as K, e.value as V)
        is IPersistentVector<*> -> when {
            e.count != 2 -> throw IllegalArgumentException(
                "Vector $e count should be 2 to conj in a map")
            else -> assoc(e.nth(0) as K, e.nth(1) as V)
        }
        else -> {
            var result: IPersistentMap<K, V> = this
            var seq = toSeq<Any?>(e) as ISeq<Any?>

            for (i in 0 until seq.count) {
                val entry = seq.first()

                if (entry !is Entry<*, *>)
                    throw IllegalArgumentException(
                        "All elements of the seq must be of type Map.Entry " +
                            "to conj: $entry")

                result = result.assoc(entry.key as K, entry.value as V)
                seq = seq.rest()
            }

            result
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun equiv(other: Any?): Boolean {
        when {
            other !is Map<*, *> -> return false
            other is IPersistentMap<*, *> &&
                other !is MapEquivalence -> return false
            count != other.size -> return false
            else -> {
                var seq = seq()
                val map = other as Map<K, V>

                for (i in 0 until count) {
                    val entry = seq.first() as Entry<K, V>
                    val key = entry.key
                    val keyFound = map.containsKey(key)

                    if (!keyFound || !equiv(entry.value, map.getValue(key)))
                        return false

                    seq = seq.rest()
                }

                return true
            }
        }
    }

    operator fun invoke(
        key: @UnsafeVariance K, default: @UnsafeVariance V?
    ): V? = valAt(key, default)

    operator fun invoke(key: @UnsafeVariance K): V? = valAt(key)

    // Map implementation
    override val size: Int
        get() = count

    override fun isEmpty(): Boolean = count == 0

    override fun containsValue(value: @UnsafeVariance V): Boolean =
        values.contains(value)

    override fun get(key: @UnsafeVariance K): V? = valAt(key)

    override val keys: Set<K>
        get() = object : AbstractSet<K>(), Set<K> {
            override val size: Int
                get() = count

            override fun contains(element: @UnsafeVariance K): Boolean =
                containsKey(element)

            override fun iterator(): Iterator<K> {
                val mapIter = this@APersistentMap.iterator()

                return object : Iterator<K> {

                    override fun hasNext(): Boolean = mapIter.hasNext()

                    override fun next(): K = mapIter.next().key
                }
            }
        }

    override val values: Collection<V>
        get() = object : AbstractCollection<V>() {

            override val size: Int
                get() = count

            override fun iterator(): Iterator<V> {
                val mapIter = this@APersistentMap.iterator()

                return object : Iterator<V> {

                    override fun hasNext(): Boolean = mapIter.hasNext()

                    override fun next(): V = mapIter.next().value
                }
            }
        }

    override val entries: Set<Entry<K, V>>
        get() = object : AbstractSet<Entry<K, V>>() {
            override val size: Int
                get() = count

            @Suppress("USELESS_IS_CHECK")
            override fun contains(
                element: Entry<@UnsafeVariance K, @UnsafeVariance V>
            ): Boolean = when (element) {
                !is Entry<K, V> -> false
                else -> {
                    val e = entryAt(element.key)
                    e != null && e.value == element.value
                }
            }

            override fun hashCode(): Int = this@APersistentMap.hashCode()

            override fun iterator(): Iterator<Entry<K, V>> =
                this@APersistentMap.iterator()
        }
}