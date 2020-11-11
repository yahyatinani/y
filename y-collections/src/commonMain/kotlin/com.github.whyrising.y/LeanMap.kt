package com.github.whyrising.y

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic

class LeanMap {
    interface Node<out K, out V> {
        val array: Array<Any?>

        fun assoc(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V>

        fun without(
            isMutable: AtomicBoolean,
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
            default: @UnsafeVariance V
        ): V

        fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
        ): IMapEntry<K, V>?

        fun nodeSeq(): ISeq<MapEntry<K, V>>
    }

    internal sealed class BitMapIndexedNode<out K, out V>(
        val isMutable: AtomicBoolean,
        val datamap: Int,
        val nodemap: Int,
        override val array: Array<Any?>
    ) : Node<K, V> {

        fun mergeIntoSubNode(isMutable: AtomicBoolean,
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
                    isMutable,
                    currentHash,
                    2,
                    arrayOf(currentKey, currentValue, key, value))
            else {
                val currentMask = mask(currentHash, shift)
                val newMask = mask(newHash, shift)

                when (currentMask) {
                    newMask -> {
                        val subNode = mergeIntoSubNode(isMutable,
                            shift + 5,
                            currentHash,
                            currentKey,
                            currentValue,
                            newHash,
                            key,
                            value)

                        return BMIN(
                            isMutable,
                            0,
                            bitpos(currentHash, shift),
                            arrayOf(subNode))
                    }
                    else -> return BMIN(
                        isMutable,
                        bitpos(currentHash, shift) or bitpos(newHash, shift),
                        0,
                        if (currentMask < newMask)
                            arrayOf(currentKey, currentValue, key, value)
                        else arrayOf(key, value, currentKey, currentValue))
                }
            }
        }

        private fun putNewNode(
            isMutable: AtomicBoolean,
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
                isMutable,
                datamap xor bitpos,
                nodemap or bitpos,
                newArray
            )
        }

        internal fun updateArrayByIndex(
            index: Int, value: Any?, isMutable: AtomicBoolean
        ): BitMapIndexedNode<K, V> =
            // TODO: Review : Consider using the thread id instead of isMutable
            if (assertMutable(this.isMutable, isMutable)) {
                array[index] = value
                this
            } else {
                val newArray = array.copyOf()
                newArray[index] = value
                BMIN(isMutable, datamap, nodemap, newArray)
            }

        private fun nodeIndexBy(bitpos: Int) =
            array.size - 1 - bitmapNodeIndex(nodemap, bitpos)

        @Suppress("UNCHECKED_CAST")
        @ExperimentalStdlibApi
        override fun assoc(
            isMutable: AtomicBoolean,
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
                        return updateArrayByIndex(keyIdx + 1, value, isMutable)

                    val currentValue = array[keyIdx + 1] as V

                    val subNode = mergeIntoSubNode(
                        isMutable,
                        shift + 5,
                        hasheq(currentKey),
                        currentKey,
                        currentValue,
                        hasheq(key),
                        key,
                        value)

                    leafFlag.value = leafFlag

                    return putNewNode(isMutable, bitpos, subNode)
                }
                (nodemap and bitpos) != 0 -> {
                    val nodeIdx = nodeIndexBy(bitpos)
                    val subNode = array[nodeIdx] as BitMapIndexedNode<K, V>

                    val newNode = subNode.assoc(
                        isMutable, shift + 5, keyHash, key, value, leafFlag)

                    if (subNode == newNode) return this

                    return updateArrayByIndex(nodeIdx, newNode, isMutable)
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

                    return BMIN(isMutable, (datamap or bitpos), nodemap, newArr)
                }
            }
        }

        private fun copyAndRemove(
            index: Int, isMutable: AtomicBoolean, bitpos: Int): BMIN<K, V> {
            val newArray = arrayOfNulls<Any?>(array.size - 2)

            array.copyInto(newArray, 0, 0, index)
            array.copyInto(newArray, index, index + 2, array.size)

            return BMIN(isMutable, datamap xor bitpos, nodemap, newArray)
        }

        private fun copyAndInlinePair(
            isMutable: AtomicBoolean, bitpos: Int, node: Node<K, V>
        ): Node<K, V> {
            val oldIndex = array.size - 1 - bitmapNodeIndex(nodemap, bitpos)
            val newIndex = 2 * bitmapNodeIndex(datamap, bitpos)
            val newArray = arrayOfNulls<Any?>(array.size + 1)

            array.copyInto(newArray, 0, 0, newIndex)
            newArray[newIndex] = node.array[0]
            newArray[newIndex + 1] = node.array[1]
            array.copyInto(newArray, newIndex + 2, newIndex, oldIndex)
            array.copyInto(newArray, oldIndex + 2, oldIndex + 1, array.size)

            return BMIN(isMutable, datamap or bitpos, nodemap xor bitpos, newArray)
        }

        @Suppress("UNCHECKED_CAST")
        override fun without(
            isMutable: AtomicBoolean,
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
                        val newDatamap = if (shift == 0) (datamap xor bitpos)
                        else bitpos(keyHash, 0)

                        return if (bmnIndex == 0)
                            BMIN(isMutable,
                                newDatamap,
                                0,
                                arrayOf(array[2], array[3]))
                        else
                            BMIN(isMutable,
                                newDatamap,
                                0,
                                arrayOf(array[0], array[1]))
                    }
                    return copyAndRemove(index, isMutable, bitpos)
                } else return this
            }

            if ((nodemap and bitpos) != 0) {
                val nodeIndex = nodeIndexBy(bitpos)

                val subNode = array[nodeIndex] as Node<K, V>
                val newSubNode = subNode.without(
                    isMutable,
                    shift + 5,
                    keyHash,
                    key,
                    removedLeaf)

                when {
                    subNode != newSubNode -> return when {
                        newSubNode.isSingleKV() -> when {
                            (datamap == 0) && nodemap.countOneBits() == 1 ->
                                newSubNode
                            else ->
                                copyAndInlinePair(isMutable, bitpos, newSubNode)
                        }
                        else ->
                            updateArrayByIndex(nodeIndex, newSubNode, isMutable)
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
            default: @UnsafeVariance V
        ): V = bitpos(keyHash, shift).let { bitpos ->
            when {
                (datamap and bitpos) != 0 -> {
                    val keyIndex = 2 * bitmapNodeIndex(datamap, bitpos)

                    when (array[keyIndex]) {
                        key -> array[keyIndex + 1] as V
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
                    array, 0, nodes, cursorLengths, 0, dataArity() - 1)
            }
        }

        object EmptyBitMapIndexedNode : BitMapIndexedNode<Nothing, Nothing>(
            atomic(false), 0, 0, emptyArray())

        internal class BMIN<out K, out V>(
            isMutable: AtomicBoolean,
            datamap: Int,
            nodemap: Int,
            array: Array<Any?>
        ) : BitMapIndexedNode<K, V>(isMutable, datamap, nodemap, array)

        companion object {

            operator fun <K, V> invoke(): BitMapIndexedNode<K, V> =
                EmptyBitMapIndexedNode

            fun bitmapNodeIndex(bitmap: Int, bitpos: Int): Int =
                (bitmap and (bitpos - 1)).countOneBits()

            fun assertMutable(x: AtomicBoolean, y: AtomicBoolean) =
                x == y && x.value
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

        override fun rest(): ISeq<MapEntry<K, V>> = createNodeSeq(
            array, lvl, nodes, cursorLengths, dataIndex, dataLength)
    }

    internal class HashCollisionNode<out K, out V>(
        val isMutable: AtomicBoolean,
        val hash: Int,
        val count: Int,
        override val array: Array<Any?>
    ) : Node<K, V> {

        override fun assoc(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V> {
            TODO("Not yet implemented")
        }

        override fun without(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            removedLeaf: Box
        ): Node<K, V> {
            TODO("Not yet implemented")
        }

        override fun hasNodes(): Boolean = false

        override fun hasData(): Boolean = true

        override fun nodeArity(): Int {
            TODO("Not yet implemented")
        }

        override fun dataArity(): Int {
            TODO("Not yet implemented")
        }

        override fun getNode(nodeIndex: Int): Node<K, V> {
            TODO("Not yet implemented")
        }

        override fun isSingleKV(): Boolean = count == 1

        override fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            default: @UnsafeVariance V
        ): V {
            TODO("Not yet implemented")
        }

        override fun find(
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K
        ): IMapEntry<K, V>? {
            TODO("Not yet implemented")
        }

        override fun nodeSeq(): ISeq<MapEntry<K, V>> {
            TODO("Not yet implemented")
        }
    }

    companion object {
//        internal val NOT_FOUND_TOKEN = Any()

        fun mask(hash: Int, shift: Int): Int = (hash ushr shift) and 0x01f

        fun bitpos(hash: Int, shift: Int): Int = 1 shl mask(hash, shift)

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
                                        node.dataArity() - 1)

                                level++
                            }
                        }
                    }
                    return emptySeq()
                }
            }
        }
    }
}
