package com.github.whyrising.y

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic

class LeanMap {

    interface Node<out K, out V> {
        fun assoc(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            pair: Pair<@UnsafeVariance K, @UnsafeVariance V>,
            leafFlag: Box
        ): Node<K, V>
    }

    sealed class BitMapIndexedNode<out K, out V>(
        val isMutable: AtomicBoolean,
        val datamap: Int,
        val nodemap: Int,
        val array: Array<Any?>
    ) : Node<K, V> {

        override fun assoc(
            isMutable: AtomicBoolean,
            shift: Int,
            keyHash: Int,
            pair: Pair<@UnsafeVariance K, @UnsafeVariance V>,
            leafFlag: Box
        ): Node<K, V> {
            val bitpos = bitpos(keyHash, shift)

            val arrSize = array.size
            val index = (2 * bitmapNodeIndex(datamap, bitpos))

            val newArray: Array<Any?> = arrayOfNulls(arrSize + 2)
            array.copyInto(newArray, 0, 0, index)
            newArray[index] = pair.first
            newArray[index + 1] = pair.second
            array.copyInto(newArray, index + 2, index, arrSize - index)
            leafFlag.value = leafFlag

            return BMIN(isMutable, datamap or bitpos, nodemap, newArray)
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
        }
    }

    companion object {
        fun mask(hash: Int, shift: Int): Int = (hash ushr shift) and 0x01f

        fun bitpos(hash: Int, shift: Int): Int = 1 shl mask(hash, shift)
    }
}
