package com.github.whyrising.y

import com.github.whyrising.y.PersistentVector.Node.EmptyNode

const val SHIFT = 5
const val BF = 32

sealed class PersistentVector<out E>(
    internal val count: Int,
    internal val shift: Int,
    internal val root: Node<E>,
    internal val tail: Array<Any?>
) : IPersistentVector<E> {

    private fun pushTail(level: Int, parent: Node<E>, tail: Node<E>): Node<E> {
        val rootNode = Node<E>(parent.array.copyOf())
        val index = ((count - 1) ushr level) and 0x01f
        rootNode.array[index] = tail

        return rootNode
    }

    override fun conj(e: @UnsafeVariance E): PersistentVector<E> {
        // empty slot available in tail?
        if (count - tailOffset(count) < BF) {
            val newTail = tail.copyOf(tail.size + 1)
            newTail[tail.size] = e

            return Vector(count + 1, SHIFT, root, newTail)
        }

        val tailNode = Node<E>(tail)
        var newShift = shift
        val newRoot: Node<E>

        when {
            // root overflow?
            (count ushr SHIFT) > (1 shl shift) -> {
                newRoot = Node()
                newRoot.array[0] = root
                newRoot.array[1] = newPath(shift, tailNode)
                newShift += SHIFT
            }
            else -> newRoot = pushTail(shift, root, tailNode)
        }

        return Vector(count + 1, newShift, newRoot, arrayOf(e))
    }

    internal sealed class Node<out T>(val array: Array<Any?>) {
        internal class Node2<out T>(_array: Array<Any?>) :
            Node<T>(_array)

        internal object EmptyNode :
            PersistentVector.Node<Nothing>(arrayOfNulls(BF))

        companion object {
            operator fun <T> invoke(): Node2<T> = Node2(arrayOfNulls(BF))

            operator fun <T> invoke(nodes: Array<Any?>): Node2<T> = Node2(nodes)
        }
    }

    internal abstract class AEmptyVector<E> : PersistentVector<E>(
        0,
        SHIFT,
        EmptyNode,
        arrayOfNulls(0)
    ) {
        override fun toString(): String = "[]"
    }

    internal object EmptyVector : AEmptyVector<Nothing>()

    internal class Vector<out E>(
        _count: Int,
        _shift: Int,
        _root: Node<E>,
        _tail: Array<Any?>
    ) : PersistentVector<E>(_count, _shift, _root, _tail)

    companion object {
        operator fun <E> invoke(): PersistentVector<E> = EmptyVector

        @Suppress("UNCHECKED_CAST")
        operator fun <E> invoke(vararg args: E): PersistentVector<E> {
            val argsCount = args.size

            return when {
                argsCount == 0 -> EmptyVector
                argsCount <= BF -> {
                    val tail = args as Array<Any?>
                    Vector(argsCount, SHIFT, EmptyNode, tail)
                }
                else -> // TODO: reimplement using TransientVector
                    args.fold<E, PersistentVector<E>>(EmptyVector) { acc, e ->
                        acc.conj(e)
                    }
            }
        }

        private fun tailOffset(count: Int): Int = when {
            count < BF -> 0
            else -> ((count - 1) ushr SHIFT) shl SHIFT
        }

        private tailrec fun <E> newPath(level: Int, node: Node<E>): Node<E> {
            if (level == 0) return node

            val path = Node<E>()
            path.array[0] = node

            return newPath(level - SHIFT, path)
        }
    }
}
