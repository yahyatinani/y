package com.github.whyrising.y

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic

class LeanMap {

    interface Node<out K, out V> {
        fun assoc(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V>
    }

    sealed class BitMapIndexedNode<out K, out V>(
        val isMutable: AtomicBoolean,
        val datamap: Int,
        val nodemap: Int,
        val array: Array<Any?>
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

        @Suppress("UNCHECKED_CAST")
        @ExperimentalStdlibApi
        override fun assoc(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            key: @UnsafeVariance K,
            value: @UnsafeVariance V,
            leafFlag: Box
        ): Node<K, V> {
            val bitpos = bitpos(keyHash, shift)

            if ((datamap and bitpos) != 0) {
                val index = bitmapNodeIndex(datamap, bitpos)
                val keyIndex = 2 * index
                val currentKey = array[keyIndex] as K

                if (equiv(currentKey, key))
                    return updateArrayByIndex(keyIndex + 1, value, isMutable)

                val currentValue = array[keyIndex + 1] as V

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
            } else if ((nodemap and bitpos) != 0) {
                val nodeIdx = array.size - 1 - bitmapNodeIndex(nodemap, bitpos)
                val subNode = array[nodeIdx] as BitMapIndexedNode<K, V>

                val newNode = subNode.assoc(
                    isMutable, shift + 5, keyHash, key, value, leafFlag)

                if (subNode == newNode) return this

                return updateArrayByIndex(nodeIdx, newNode, isMutable)
            } else {
                val arraySize = array.size
                val index = (2 * bitmapNodeIndex(datamap, bitpos))

                val newArray: Array<Any?> = arrayOfNulls(arraySize + 2)
                array.copyInto(newArray, 0, 0, index)
                newArray[index] = key
                newArray[index + 1] = value
                array.copyInto(newArray, index + 2, index, arraySize)
                leafFlag.value = leafFlag

                return BMIN(isMutable, (datamap or bitpos), nodemap, newArray)
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

    class HashCollisionNode<out K, out V>(
        val isMutable: AtomicBoolean,
        val hash: Int,
        val count: Int,
        val array: Array<Any?>
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
    }

    companion object {
        fun mask(hash: Int, shift: Int): Int = (hash ushr shift) and 0x01f

        fun bitpos(hash: Int, shift: Int): Int = 1 shl mask(hash, shift)
    }
}
