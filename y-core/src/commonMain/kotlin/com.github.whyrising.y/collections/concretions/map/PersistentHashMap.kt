package com.github.whyrising.y.collections.concretions.map

import com.github.whyrising.y.collections.Edit
import com.github.whyrising.y.collections.concretions.list.ASeq
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.BitMapIndexedNode.EmptyBitMapIndexedNode
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.NodeIterator.EmptyNodeIterator
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.NodeIterator.NodeIter
import com.github.whyrising.y.collections.map.APersistentMap
import com.github.whyrising.y.collections.map.IMapEntry
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.map.MapIterable
import com.github.whyrising.y.collections.mutable.collection.IMutableCollection
import com.github.whyrising.y.collections.mutable.map.ATransientMap
import com.github.whyrising.y.collections.mutable.map.TransientMap
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.util.Box
import com.github.whyrising.y.util.equiv
import com.github.whyrising.y.util.hasheq
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class PersistentHashMapSerializer<K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
) : KSerializer<PersistentHashMap<K, V>> {
    internal val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): PersistentHashMap<K, V> =
        PersistentHashMap.create(mapSerializer.deserialize(decoder))
            as PersistentHashMap<K, V>

    override fun serialize(encoder: Encoder, value: PersistentHashMap<K, V>) {
        return mapSerializer.serialize(encoder, value)
    }
}

@Serializable(with = PersistentHashMapSerializer::class)
sealed class PersistentHashMap<out K, out V>(
    override val count: Int,
    val root: Node<K, V>?
) : APersistentMap<K, V>(), IMutableCollection<Any?>, MapIterable<K, V> {
    override fun assoc(
        key: @UnsafeVariance K,
        value: @UnsafeVariance V
    ): IPersistentMap<K, V> {
        val addedLeaf = Box(null)
        val newRoot = (root ?: EmptyBitMapIndexedNode)
            .assoc(Edit(null), 0, hasheq(key), key, value, addedLeaf)

        if (newRoot == this.root) return this

        return LMap(if (addedLeaf.value == null) count else count + 1, newRoot)
    }

    override fun assocNew(
        key: @UnsafeVariance K,
        value: @UnsafeVariance V
    ): IPersistentMap<K, V> = when {
        containsKey(key) -> throw RuntimeException(
            "The key $key is already present."
        )
        else -> assoc(key, value)
    }

    override fun asTransient(): TransientMap<K, V> = TransientLeanMap(this)

    override fun empty(): IPersistentCollection<Any?> = EmptyHashMap

    abstract
    class AEmptyHashMap<out K, out V> : PersistentHashMap<K, V>(0, null) {

        override fun toString(): String = "{}"

        override fun dissoc(key: @UnsafeVariance K): IPersistentMap<K, V> = this

        override fun containsKey(key: @UnsafeVariance K): Boolean = false

        override fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>? = null

        override
        fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V? =
            default

        override fun valAt(key: @UnsafeVariance K): V? = null

        override fun seq(): ISeq<Any?> = PersistentList.Empty

        override fun iterator(): Iterator<Map.Entry<K, V>> =
            EmptyNodeIterator

        override fun keyIterator(): Iterator<K> = EmptyNodeIterator

        override fun valIterator(): Iterator<V> = EmptyNodeIterator
    }

    object EmptyHashMap : AEmptyHashMap<Nothing, Nothing>()

    internal class LMap<out K, out V>(
        private val _count: Int,
        private val _root: Node<K, V>
    ) : PersistentHashMap<K, V>(_count, _root) {

        override fun dissoc(key: @UnsafeVariance K): IPersistentMap<K, V> {
            val newRoot = _root.without(
                Edit(null), 0, hasheq(key), key, Box(null)
            )

            if (newRoot == _root)
                return this

            return when (val newCount = _count - 1) {
                0 -> EmptyHashMap
                else -> LMap(newCount, newRoot)
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun containsKey(key: @UnsafeVariance K): Boolean =
            _root.find(0, hasheq(key), key, NOT_FOUND as V) != NOT_FOUND

        override fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>? {
            return _root.find(0, hasheq(key), key)
        }

        override
        fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V? =
            _root.find(0, hasheq(key), key, default)

        override fun valAt(key: @UnsafeVariance K): V? = valAt(key, null)

        override fun seq(): ISeq<Any?> = _root.nodeSeq()

        override fun iterator(): Iterator<Map.Entry<K, V>> =
            NodeIter(_root, makeMapEntry)

        override fun keyIterator(): Iterator<K> =
            NodeIter(_root, makeKey)

        override fun valIterator(): Iterator<V> = NodeIter(_root, makeValue)
    }

    interface Node<out K, out V> {
        val array: Array<Any?>

        fun assoc(
            edit: Edit,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V>

        fun without(
            edit: Edit,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            removedLeaf: Box
        ): Node<K, V>

        fun hasNodes(): Boolean

        fun hasData(): Boolean

        fun nodeArity(): Int

        fun dataArity(): Int

        fun getNode(nodeIndex: Int): Node<K, V>

        fun isSingleKV(): Boolean

        fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            default: @UnsafeVariance V?
        ): V?

        fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
        ): IMapEntry<K, V>?

        fun nodeSeq(): ISeq<MapEntry<K, V>>
    }

    internal sealed class NodeIterator<K, V, R>(
        val node: Node<K, V>?,
        val f: (k: K, v: V) -> R
    ) : Iterator<R> {

        object EmptyNodeIterator : NodeIterator<Nothing, Nothing, Nothing>(
            null, { _, _ -> throw RuntimeException() }
        ) {
            override fun hasNext(): Boolean = false

            override fun next(): Nothing = throw NoSuchElementException()
        }

        @Suppress("UNCHECKED_CAST")
        class NodeIter<K, V, R>(
            _node: Node<K, V>,
            val _f: (k: K, v: V) -> R
        ) : NodeIterator<K, V, R>(_node, _f) {
            private val _null = Any()
            private var nextEntry: R = _null as R

            val nodes = arrayOfNulls<Node<K, V>>(7)
            private val cursorLengths = arrayOfNulls<Int>(7)

            var array: Array<Any?> = _node.array
            private var lvl: Int = 0
            private var dataIndex: Int = 0
            private var dataLength: Int = _node.dataArity()

            init {
                nodes[0] = _node
                cursorLengths[0] = _node.nodeArity()

                when (dataLength) {
                    0 -> advance()
                    else -> dataLength--
                }

                val k = 2 * dataIndex
                nextEntry = _f(array[k] as K, array[k + 1] as V)
            }

            private fun advance(): Boolean {
                if (dataIndex < dataLength) {
                    dataIndex++
                    val k = 2 * dataIndex
                    nextEntry = _f(array[k] as K, array[k + 1] as V)

                    return true
                } else {
                    while (lvl >= 0)
                        when (val nodeIndex = cursorLengths[lvl]) {
                            0 -> lvl--
                            else -> {
                                cursorLengths[lvl] = nodeIndex!! - 1

                                val n = nodes[lvl]!!.getNode(nodeIndex)
                                val hasNodes = n.hasNodes()
                                val newLvl = if (hasNodes) lvl + 1 else lvl

                                if (hasNodes) {
                                    nodes[newLvl] = n
                                    cursorLengths[newLvl] = n.nodeArity()
                                }

                                if (n.hasData()) {
                                    array = n.array
                                    lvl = newLvl
                                    dataIndex = 0
                                    dataLength = n.dataArity() - 1
                                    val k = 2 * dataIndex
                                    nextEntry = _f(
                                        array[k] as K,
                                        array[k + 1] as V
                                    )

                                    return true
                                }

                                lvl++
                            }
                        }

                    return false
                }
            }

            override fun hasNext(): Boolean = when {
                nextEntry != _null -> true
                else -> advance()
            }

            override fun next(): R {
                val r = nextEntry

                return when {
                    r != _null -> {
                        nextEntry = _null as R

                        r
                    }
                    advance() -> next()
                    else -> throw NoSuchElementException()
                }
            }
        }
    }

    /* Transients are not thread safe */
    internal class TransientLeanMap<out K, out V> private constructor(
        val edit: Edit,
        root: Node<K, V>?,
        count: Int,
        val leafFlag: Box
    ) : ATransientMap<K, V>() {

        internal constructor(map: PersistentHashMap<K, V>) : this(
            Edit(Any()),
            map.root,
            map.count,
            Box(null)
        )

        private
        val _root = atomic<Node<@UnsafeVariance K, @UnsafeVariance V>?>(root)
        private val _count = atomic(count)

        internal val root by _root
        internal val countValue by _count

        override val doCount: Int
            by _count

        override fun ensureEditable() {
            if (edit.value == null)
                throw IllegalStateException(
                    "Transient used after persistent() call."
                )
        }

        override fun doAssoc(
            key: @UnsafeVariance K,
            value: @UnsafeVariance V
        ): TransientMap<K, V> {
            leafFlag.value = null
            val node = (_root.value ?: EmptyBitMapIndexedNode).assoc(
                edit,
                0,
                hasheq(key),
                key,
                value,
                leafFlag
            )

            _root.update {
                when {
                    it != node -> node
                    else -> it
                }
            }

            if (leafFlag.value != null)
                _count.incrementAndGet()

            return this
        }

        override fun doDissoc(key: @UnsafeVariance K): TransientMap<K, V> {
            leafFlag.value = null

            val node = (_root.value ?: EmptyBitMapIndexedNode).without(
                edit,
                0,
                hasheq(key),
                key,
                leafFlag
            )

            _root.update {
                when {
                    it != node -> node
                    else -> it
                }
            }

            if (leafFlag.value != null)
                _count.decrementAndGet()

            return this
        }

        override fun doPersistent(): IPersistentMap<K, V> {
            edit.value = null

            return when (val count = _count.value) {
                0 -> EmptyHashMap
                else -> LMap(count, _root.value as Node<K, V>)
            }
        }

        override fun doValAt(
            key: @UnsafeVariance K,
            default: @UnsafeVariance V?
        ): V? = when (val value = _root.value) {
            null -> default
            else -> value.find(0, hasheq(key), key, default)
        }
    }

    internal class NodeSeq<out K, out V>(
        val array: Array<Any?>,
        val lvl: Int,
        val nodes: Array<Node<@UnsafeVariance K, @UnsafeVariance V>?>,
        val cursorLengths: Array<Int>,
        val dataIndex: Int,
        val dataLength: Int,
    ) : ASeq<MapEntry<K, V>>() {

        @Suppress("UNCHECKED_CAST")
        override fun first(): MapEntry<K, V> =
            MapEntry(array[2 * dataIndex] as K, array[2 * dataIndex + 1] as V)

        override fun next(): ISeq<MapEntry<K, V>>? = when (
            val seq = createNodeSeq(
                array,
                lvl,
                nodes,
                cursorLengths,
                dataIndex,
                dataLength
            )
        ) {
            PersistentList.Empty -> null
            else -> seq
        }
    }

    internal sealed class BitMapIndexedNode<out K, out V>(
        val edit: Edit,
        val datamap: Int,
        val nodemap: Int,
        override val array: Array<Any?>
    ) : Node<K, V> {
        fun mergeIntoSubNode(
            edit: Edit,
            shift: Int,
            currentHash: Int,
            currentKey: @UnsafeVariance K,
            currentValue: @UnsafeVariance V,
            newHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V
        ): Node<K, V> {
            if (shift > 32 && currentHash == newHash)
                return HashCollisionNode(
                    edit,
                    currentHash,
                    2,
                    arrayOf(currentKey, currentValue, key, value)
                )
            else {
                val currentMask = mask(currentHash, shift)
                val newMask = mask(newHash, shift)

                when (currentMask) {
                    newMask -> {
                        val subNode = mergeIntoSubNode(
                            edit,
                            shift + 5,
                            currentHash,
                            currentKey,
                            currentValue,
                            newHash,
                            key,
                            value
                        )
                        return BMIN(
                            edit,
                            0,
                            bitpos(currentHash, shift),
                            arrayOf(subNode)
                        )
                    }
                    else -> return BMIN(
                        edit,
                        bitpos(currentHash, shift) or bitpos(newHash, shift),
                        0,
                        if (currentMask < newMask)
                            arrayOf(currentKey, currentValue, key, value)
                        else arrayOf(key, value, currentKey, currentValue)
                    )
                }
            }
        }

        private fun putNewNode(
            edit: Edit,
            bitpos: Int,
            subNode: Node<K, V>
        ): Node<K, V> {
            val oldIdx = 2 * bitmapNodeIndex(datamap, bitpos)
            val newIdx = array.size - 2 - bitmapNodeIndex(nodemap, bitpos)
            val newArray = arrayOfNulls<Any?>(array.size - 1)

            val endIndex = newIdx + 2
            array.copyInto(newArray, 0, 0, oldIdx)
            array.copyInto(newArray, oldIdx, oldIdx + 2, endIndex)
            newArray[newIdx] = subNode
            array.copyInto(newArray, newIdx + 1, endIndex, array.size)

            return BMIN(
                edit,
                datamap xor bitpos,
                nodemap or bitpos,
                newArray
            )
        }

        internal fun updateArrayByIndex(
            index: Int,
            value: Any?,
            edit: Edit
        ): BitMapIndexedNode<K, V> =
            // TODO: Review : Consider using the thread id instead of isMutable
            if (isAllowedToEdit(this.edit, edit)) {
                array[index] = value
                this
            } else {
                val newArray = array.copyOf()
                newArray[index] = value
                BMIN(edit, datamap, nodemap, newArray)
            }

        private fun nodeIndexBy(bitpos: Int) =
            array.size - 1 - bitmapNodeIndex(nodemap, bitpos)

        @Suppress("UNCHECKED_CAST")
        override fun assoc(
            edit: Edit,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V> = bitpos(keyHash, shift).let { bitpos ->
            when {
                (datamap and bitpos) != 0 -> {
                    val index = bitmapNodeIndex(datamap, bitpos)
                    val keyIdx = 2 * index
                    val currentKey = array[keyIdx] as K

                    if (equiv(currentKey, key))
                        return updateArrayByIndex(keyIdx + 1, value, edit)

                    val currentValue = array[keyIdx + 1] as V

                    val subNode = mergeIntoSubNode(
                        edit,
                        shift + 5,
                        hasheq(currentKey),
                        currentKey,
                        currentValue,
                        hasheq(key),
                        key,
                        value
                    )
                    leafFlag.value = leafFlag

                    return putNewNode(edit, bitpos, subNode)
                }
                (nodemap and bitpos) != 0 -> {
                    val nodeIdx = nodeIndexBy(bitpos)
                    val subNode = array[nodeIdx] as BitMapIndexedNode<K, V>

                    val newNode = subNode.assoc(
                        edit, shift + 5, keyHash, key, value, leafFlag
                    )

                    if (subNode == newNode) return this

                    return updateArrayByIndex(nodeIdx, newNode, edit)
                }
                else -> {
                    val arraySize = array.size
                    val index = (2 * bitmapNodeIndex(datamap, bitpos))

                    val newArr: Array<Any?> = arrayOfNulls(arraySize + 2)
                    array.copyInto(newArr, 0, 0, index)
                    newArr[index] = key
                    newArr[index + 1] = value
                    array.copyInto(newArr, index + 2, index, arraySize)
                    leafFlag.value = leafFlag

                    return BMIN(edit, (datamap or bitpos), nodemap, newArr)
                }
            }
        }

        private fun copyAndRemove(
            index: Int,
            edit: Edit,
            bitpos: Int
        ): BMIN<K, V> {
            val newArray = arrayOfNulls<Any?>(array.size - 2)

            array.copyInto(newArray, 0, 0, index)
            array.copyInto(newArray, index, index + 2, array.size)

            return BMIN(edit, datamap xor bitpos, nodemap, newArray)
        }

        private fun copyAndInlinePair(
            edit: Edit,
            bitpos: Int,
            node: Node<K, V>
        ): Node<K, V> {
            val oldIndex = array.size - 1 - bitmapNodeIndex(nodemap, bitpos)
            val newIndex = 2 * bitmapNodeIndex(datamap, bitpos)
            val newArray = arrayOfNulls<Any?>(array.size + 1)

            array.copyInto(newArray, 0, 0, newIndex)
            newArray[newIndex] = node.array[0]
            newArray[newIndex + 1] = node.array[1]
            array.copyInto(newArray, newIndex + 2, newIndex, oldIndex)
            array.copyInto(newArray, oldIndex + 2, oldIndex + 1, array.size)

            return BMIN(edit, datamap or bitpos, nodemap xor bitpos, newArray)
        }

        @Suppress("UNCHECKED_CAST")
        override fun without(
            edit: Edit,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            removedLeaf: Box
        ): Node<K, V> {
            val bitpos = bitpos(keyHash, shift)

            if ((datamap and bitpos) != 0) {
                val bmnIndex = bitmapNodeIndex(datamap, bitpos)
                val index = 2 * bmnIndex
                if (equiv(key, array[index])) {
                    removedLeaf.value = removedLeaf
                    if ((datamap.countOneBits() == 2) && nodemap == 0) {
                        val newDatamap = when (shift) {
                            0 -> (datamap xor bitpos)
                            else -> bitpos(keyHash, 0)
                        }

                        return when (bmnIndex) {
                            0 -> BMIN(
                                edit,
                                newDatamap,
                                0,
                                arrayOf(array[2], array[3])
                            )
                            else -> BMIN(
                                edit,
                                newDatamap,
                                0,
                                arrayOf(array[0], array[1])
                            )
                        }
                    }
                    return copyAndRemove(index, edit, bitpos)
                } else return this
            }

            if ((nodemap and bitpos) != 0) {
                val nodeIndex = nodeIndexBy(bitpos)

                val subNode = array[nodeIndex] as Node<K, V>
                val newSubNode = subNode.without(
                    edit,
                    shift + 5,
                    keyHash,
                    key,
                    removedLeaf
                )

                when {
                    subNode != newSubNode -> return when {
                        newSubNode.isSingleKV() -> when {
                            (datamap == 0) && nodemap.countOneBits() == 1 ->
                                newSubNode
                            else ->
                                copyAndInlinePair(edit, bitpos, newSubNode)
                        }
                        else ->
                            updateArrayByIndex(nodeIndex, newSubNode, edit)
                    }
                }
            }

            return this
        }

        override fun hasNodes(): Boolean = nodemap != 0

        override fun hasData(): Boolean = datamap != 0

        override fun nodeArity(): Int = nodemap.countOneBits()

        override fun dataArity(): Int = datamap.countOneBits()

        override fun isSingleKV(): Boolean =
            (nodemap == 0) && (datamap.countOneBits() == 1)

        @Suppress("UNCHECKED_CAST")
        override fun getNode(nodeIndex: Int): Node<K, V> =
            array[array.size - nodeIndex] as Node<K, V>

        @Suppress("UNCHECKED_CAST")
        override fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            default: @UnsafeVariance V?
        ): V? = bitpos(keyHash, shift).let { bitpos ->
            when {
                (datamap and bitpos) != 0 -> {
                    val keyIndex = 2 * bitmapNodeIndex(datamap, bitpos)
                    when {
                        equiv(array[keyIndex], key) -> array[keyIndex + 1] as V
                        else -> default
                    }
                }
                (nodemap and bitpos) != 0 ->
                    (array[nodeIndexBy(bitpos)] as Node<K, V>)
                        .find(shift + 5, keyHash, key, default)
                else -> default
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K
        ): IMapEntry<K, V>? = bitpos(keyHash, shift).let { bitpos ->
            when {
                (datamap and bitpos) != 0 -> {
                    val keyIndex = 2 * bitmapNodeIndex(datamap, bitpos)

                    when (array[keyIndex]) {
                        key -> MapEntry(key, array[keyIndex + 1] as V)
                        else -> null
                    }
                }
                (nodemap and bitpos) != 0 ->
                    (array[nodeIndexBy(bitpos)] as Node<K, V>)
                        .find(shift + 5, keyHash, key)
                else -> null
            }
        }

        override fun nodeSeq(): ISeq<MapEntry<K, V>> {
            val nodes = arrayOfNulls<Node<K, V>?>(7)
            val cursorLengths = arrayOf(0, 0, 0, 0, 0, 0, 0)
            nodes[0] = this
            cursorLengths[0] = nodeArity()

            return when (datamap) {
                0 -> createNodeSeq(array, 0, nodes, cursorLengths, 0, 0)
                else -> NodeSeq(
                    array, 0, nodes, cursorLengths, 0, dataArity() - 1
                )
            }
        }

        object EmptyBitMapIndexedNode : BitMapIndexedNode<Nothing, Nothing>(
            Edit(null), 0, 0, emptyArray()
        )

        internal class BMIN<out K, out V>(
            edit: Edit,
            datamap: Int,
            nodemap: Int,
            array: Array<Any?>
        ) : BitMapIndexedNode<K, V>(edit, datamap, nodemap, array)

        companion object {

            operator fun <K, V> invoke(): BitMapIndexedNode<K, V> =
                EmptyBitMapIndexedNode

            fun bitmapNodeIndex(bitmap: Int, bitpos: Int): Int =
                (bitmap and (bitpos - 1)).countOneBits()
        }
    }

    internal class HashCollisionNode<out K, out V>(
        val edit: Edit,
        val hash: Int,
        var count: Int,
        override var array: Array<Any?>
    ) : Node<K, V> {

        internal fun mutableAssoc(
            index: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): HashCollisionNode<K, V> {
            when (index) {
                -1 -> {
                    val newArray = arrayOfNulls<Any?>(array.size + 2)
                    array.copyInto(newArray, 0, 0, array.size)
                    newArray[array.size] = key
                    newArray[array.size + 1] = value
                    leafFlag.value = leafFlag

                    array = newArray
                    count++
                }
                else -> if (value != array[index + 1]) array[index + 1] = value
            }

            return this
        }

        private fun persistentAssoc(
            index: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V> = when (index) {
            -1 -> {
                val newArray = arrayOfNulls<Any?>(array.size + 2)
                array.copyInto(newArray, 0, 0, array.size)
                newArray[array.size] = key
                newArray[array.size + 1] = value
                leafFlag.value = leafFlag

                HashCollisionNode(this.edit, hash, count + 1, newArray)
            }
            else -> when (value) {
                array[index + 1] -> {
                    this
                }
                else -> {
                    val newArray = array.copyOf()
                    newArray[index + 1] = value

                    HashCollisionNode(this.edit, hash, count, newArray)
                }
            }
        }

        override fun assoc(
            edit: Edit,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V> = findIndexBy(key).let { index ->
            when {
                isAllowedToEdit(this.edit, edit) -> {
                    val newNode = when (this.edit) {
                        edit -> this
                        else -> HashCollisionNode(
                            edit,
                            this.hash,
                            this.count,
                            this.array.copyOf()
                        )
                    }
                    newNode.mutableAssoc(index, key, value, leafFlag)
                }
                else -> persistentAssoc(index, key, value, leafFlag)
            }
        }

        private fun removePair(keyIndex: Int): Array<Any?> {
            val newArray = arrayOfNulls<Any?>(array.size - 2)

            array.copyInto(newArray, 0, 0, keyIndex)
            array.copyInto(newArray, keyIndex, keyIndex + 2, array.size)

            return newArray
        }

        @Suppress("UNCHECKED_CAST")
        override fun without(
            edit: Edit,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            removedLeaf: Box
        ): Node<K, V> {
            val index = findIndexBy(key)

            if (index == -1) return this

            removedLeaf.value = removedLeaf

            return when (count) {
                1 -> EmptyBitMapIndexedNode
                2 -> (if (index == 0) 2 else 0).let { remainingIdx ->
                    BitMapIndexedNode<K, V>()
                        .assoc(
                            edit,
                            0,
                            keyHash,
                            array[remainingIdx] as K,
                            array[remainingIdx + 1] as V,
                            removedLeaf
                        )
                }
                else -> HashCollisionNode(
                    edit, keyHash, count - 1, removePair(index)
                )
            }
        }

        override fun hasNodes(): Boolean = false

        override fun hasData(): Boolean = true

        override fun nodeArity(): Int = 0

        override fun dataArity(): Int = count

        override fun getNode(nodeIndex: Int): Node<K, V> =
            throw UnsupportedOperationException(
                "HashCollisionNode has no nodes!"
            )

        override fun isSingleKV(): Boolean = count == 1

        private fun findIndexBy(key: @UnsafeVariance K): Int {
            var i = 0
            while (i < 2 * count) {
                if (equiv(key, array[i])) return i

                i += 2
            }
            return -1
        }

        @Suppress("UNCHECKED_CAST")
        override fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            default: @UnsafeVariance V?
        ): V? = findIndexBy(key).let { index ->
            return when {
                index < 0 -> default
                else -> array[index + 1] as V
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K
        ): IMapEntry<K, V>? = findIndexBy(key).let { index ->
            return when {
                index < 0 -> null
                else -> MapEntry(key, array[index + 1] as V)
            }
        }

        override fun nodeSeq(): ISeq<MapEntry<K, V>> =
            throw UnsupportedOperationException(
                "HashCollisionNode has no nodes!"
            )
    }

    companion object {
        internal val NOT_FOUND = Any()

        fun mask(hash: Int, shift: Int): Int = (hash ushr shift) and 0x01f

        fun bitpos(hash: Int, shift: Int): Int = 1 shl mask(hash, shift)

        // TODO: Use a threadID to allow different refs that hold the same
        //  threadID to edit
        fun isAllowedToEdit(x: Edit, y: Edit): Boolean {
            return x.value != null && y.value != null && x === y
        }

        private fun <K, V> createNodeSeq(
            array: Array<Any?>,
            lvl: Int,
            nodes: Array<Node<K, V>?>,
            cursorLengths: Array<Int>,
            dataIndex: Int,
            dataLength: Int
        ): ISeq<MapEntry<K, V>> {
            var level = lvl
            when {
                dataIndex < dataLength -> return NodeSeq(
                    array,
                    level,
                    nodes,
                    cursorLengths,
                    dataIndex + 1,
                    dataLength
                )
                else -> {
                    while (level >= 0) {
                        when (val nodeIndex = cursorLengths[level]) {
                            0 -> level--
                            else -> {
                                val newCursorLengths = cursorLengths.copyOf()
                                newCursorLengths[level] = nodeIndex - 1

                                val node = nodes[level]!!.getNode(nodeIndex)
                                val hasNodes = node.hasNodes()
                                val newLvl = if (hasNodes) level + 1 else level

                                val newNodes = nodes.copyOf()
                                if (hasNodes) {
                                    newNodes[newLvl] = node
                                    newCursorLengths[newLvl] = node.nodeArity()
                                }

                                if (node.hasData())
                                    return NodeSeq(
                                        node.array,
                                        newLvl,
                                        newNodes,
                                        newCursorLengths,
                                        0,
                                        node.dataArity() - 1
                                    )

                                level++
                            }
                        }
                    }
                    return PersistentList.Empty
                }
            }
        }

        internal
        operator fun <K, V> invoke(): PersistentHashMap<K, V> = EmptyHashMap

        internal fun <K, V> createWithCheck(
            array: Array<Any?>
        ): PersistentHashMap<K, V> {
            var ret: TransientMap<K, V> = EmptyHashMap.asTransient()

            for (i in array.indices step 2) {
                ret = ret.assoc(array[i] as K, array[i + 1] as V)

                if (ret.count != i / 2 + 1)
                    throw IllegalArgumentException("Duplicate key: ${array[i]}")
            }

            return ret.persistent() as PersistentHashMap<K, V>
        }

        internal fun <K, V> createWithCheck(
            vararg pairs: Pair<K, V>
        ): PersistentHashMap<K, V> {
            var ret: TransientMap<K, V> = EmptyHashMap.asTransient()

            for (i in pairs.indices) {
                val (key, value) = pairs[i]

                ret = ret.assoc(key, value)

                if (ret.count != (i + 1))
                    throw IllegalArgumentException("Duplicate key: $key")
            }

            return ret.persistent() as PersistentHashMap<K, V>
        }

        internal fun <K, V> create(map: Map<K, V>): IPersistentMap<K, V> {
            var ret: TransientMap<K, V> = EmptyHashMap.asTransient()

            for (entry in map.entries)
                ret = ret.assoc(entry.key, entry.value)

            return ret.persistent()
        }

        internal fun <K, V> create(
            vararg pairs: Pair<K, V>
        ): PersistentHashMap<K, V> {
            var ret: TransientMap<K, V> = EmptyHashMap.asTransient()

            for ((k, v) in pairs)
                ret = ret.assoc(k, v)

            return ret.persistent() as PersistentHashMap<K, V>
        }

        internal
        fun <K, V> create(array: Array<Any?>): PersistentHashMap<K, V> {
            var ret: TransientMap<K, V> = EmptyHashMap.asTransient()

            for (i in array.indices step 2)
                ret = ret.assoc(array[i] as K, array[i + 1] as V)

            return ret.persistent() as PersistentHashMap<K, V>
        }
    }
}
