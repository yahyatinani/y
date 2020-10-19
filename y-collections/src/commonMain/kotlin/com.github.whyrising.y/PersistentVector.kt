package com.github.whyrising.y

import com.github.whyrising.y.PersistentVector.Node.EmptyNode
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

internal const val SHIFT = 5
internal const val BF = 32

sealed class PersistentVector<out E>(
    override val count: Int,
    internal val shift: Int,
    internal val root: Node<E>,
    internal val tail: Array<Any?>
) : APersistentVector<E>(), IMutableCollection<E> {

    @Suppress("UNCHECKED_CAST")
    private fun pushTail(level: Int, parent: Node<E>, tail: Node<E>): Node<E> {
        val rootNode = Node<E>(parent.isMutable, parent.array.copyOf())
        val subIndex = ((count - 1) ushr level) and 0x01f

        val nodeToInsert: Node<E> = if (level == SHIFT) tail
        else when (val child = parent.array[subIndex]) {
            null -> newPath(root.isMutable, level - 5, tail)
            else -> pushTail(level - SHIFT, child as Node<E>, tail)
        }

        rootNode.array[subIndex] = nodeToInsert

        return rootNode
    }

    override fun conj(e: @UnsafeVariance E): PersistentVector<E> {
        // empty slot available in tail?
        if (count - tailOffset(count) < BF) {
            val newTail = tail.copyOf(tail.size + 1)
            newTail[tail.size] = e

            return Vector(count + 1, shift, root, newTail)
        }

        val tailNode = Node<E>(root.isMutable, tail)
        var newShift = shift
        val newRoot: Node<E>

        when {
            // root overflow?
            (count ushr SHIFT) > (1 shl shift) -> {
                newRoot = Node(root.isMutable)
                newRoot.array[0] = root
                newRoot.array[1] = newPath(root.isMutable, shift, tailNode)
                newShift += SHIFT
            }
            else -> newRoot = pushTail(shift, root, tailNode)
        }

        return Vector(count + 1, newShift, newRoot, arrayOf(e))
    }

    private fun indexOutOfBounds(index: Int) = index >= count || index < 0

    @Suppress("UNCHECKED_CAST")
    private fun leafArrayBy(index: Int): Array<E> = when {
        indexOutOfBounds(index) -> throw IndexOutOfBoundsException()
        index >= tailOffset(count) -> tail as Array<E>
        else -> {
            var level = shift
            var node = root

            while (level > 0) {
                node = node.array[(index ushr level) and 0x01f] as Node<E>
                level -= SHIFT
            }

            node.array as Array<E>
        }
    }

    override fun nth(index: Int): E {
        val leaf = leafArrayBy(index)

        return leaf[index and 0x01f]
    }

    override fun nth(index: Int, default: @UnsafeVariance E): E = when {
        indexOutOfBounds(index) -> default
        else -> nth(index)
    }

    override fun asTransient(): TransientVector<E> = TransientVector(this)

    sealed class Node<out T>(
        val isMutable: AtomicBoolean,
        val array: Array<Any?>
    ) {
        internal class Node2<out T>(
            isMutable: AtomicBoolean,
            _array: Array<Any?>
        ) : Node<T>(isMutable, _array)

        internal object EmptyNode :
            PersistentVector.Node<Nothing>(atomic(false), arrayOfNulls(BF))

        companion object {
            operator fun <T> invoke(isMutable: AtomicBoolean): Node<T> =
                Node2(isMutable, arrayOfNulls(BF))

            operator fun <T> invoke(
                isMutable: AtomicBoolean,
                nodes: Array<Any?>
            ): Node<T> = Node2(isMutable, nodes)
        }
    }

    internal object EmptyVector : PersistentVector<Nothing>(
        0,
        SHIFT,
        EmptyNode,
        arrayOfNulls(0)
    ) {
        override fun toString(): String = "[]"
    }

    internal class Vector<out E>(
        _count: Int,
        _shift: Int,
        _root: Node<E>,
        _tail: Array<Any?>
    ) : PersistentVector<E>(_count, _shift, _root, _tail)

    class TransientVector<out E> private constructor(
        size: Int,
        shift: Int,
        root: Node<E>,
        tail: Array<Any?>
    ) : ConstantCount, ITransientCollection<E> {

        private val _count: AtomicInt = atomic(size)
        private val _shift: AtomicInt = atomic(shift)
        private val _root: AtomicRef<Node<E>> = atomic(mutableNode(root))
        private val _tail: AtomicRef<Array<Any?>> = atomic(tail)

        fun assertMutable() {
            if (!_root.value.isMutable.value)
                throw IllegalStateException(
                    "Transient used after persistent() call")
        }

        override val count: Int
            get() {
                assertMutable()
                return _count.value
            }

        val shift: Int
            get() = _shift.value

        val root: Node<E>
            get() = _root.value

        internal var tail: Array<Any?>
            get() = _tail.value
            set(value) {
                _tail.value = value
            }

        internal fun invalidate() {
            _root.value.isMutable.value = false
        }

        private fun assertNodeCreatedByThisVector(node: Node<E>): Node<E> =
            when (node.isMutable) {
                root.isMutable -> node
                else -> Node(root.isMutable, node.array.copyOf())
            }

        @Suppress("UNCHECKED_CAST")
        private
        fun pushTail(level: Int, parent: Node<E>, tail: Node<E>): Node<E> {
            val subIndex = ((count - 1) ushr level) and 0x01f

            val rootNode = assertNodeCreatedByThisVector(parent)

            val nodeToInsert: Node<E> = if (level == SHIFT) tail
            else when (val child = rootNode.array[subIndex]) {
                null -> newPath(root.isMutable, level - 5, tail)
                else -> pushTail(level - SHIFT, child as Node<E>, tail)
            }

            rootNode.array[subIndex] = nodeToInsert

            return rootNode
        }

        override fun conj(e: @UnsafeVariance E): TransientVector<E> {
            assertMutable()

            val oldCount = count
            // empty slot available in tail?
            if (oldCount - tailOffset(oldCount) < BF) {
                tail[oldCount and 0x01f] = e
                ++_count.value

                return this
            }

            val tailNode = Node<E>(root.isMutable, tail)
            tail = arrayOfNulls(BF)
            tail[0] = e

            var newShift = shift
            val newRoot: Node<E>
            when {
                (count ushr SHIFT) > (1 shl shift) -> {
                    newRoot = Node(root.isMutable)
                    newRoot.array[0] = root
                    newRoot.array[1] = newPath(root.isMutable, shift, tailNode)
                    newShift += SHIFT
                }
                else -> newRoot = pushTail(shift, root, tailNode)
            }

            _root.value = newRoot
            _shift.value = newShift
            ++_count.value

            return this
        }

        override fun persistent(): PersistentVector<E> {
            assertMutable()
            invalidate()

            val trimmedTail =
                arrayOfNulls<Any?>(_count.value - tailOffset(_count.value))

            _tail.value.copyInto(trimmedTail, 0, 0, trimmedTail.size)

            return Vector(_count.value, _shift.value, _root.value, trimmedTail)
        }

        companion object {
            private fun <E> mutableNode(node: Node<E>): Node<E> =
                Node(atomic(true), node.array.copyOf())

            private fun maximizeTail(tail: Array<Any?>): Array<Any?> {
                val maxTail = arrayOfNulls<Any?>(BF)

                return tail.copyInto(maxTail, 0, 0, tail.size)
            }

            operator
            fun <E> invoke(vec: PersistentVector<E>): TransientVector<E> =
                TransientVector(
                    vec.count,
                    vec.shift,
                    vec.root,
                    maximizeTail(vec.tail)
                )
        }
    }

    companion object {
        internal operator fun <E> invoke(): PersistentVector<E> = EmptyVector

        @Suppress("UNCHECKED_CAST")
        internal operator fun <E> invoke(vararg args: E): PersistentVector<E> {
            val argsCount = args.size

            return when {
                argsCount == 0 -> EmptyVector
                argsCount <= BF -> {
                    val tail = args as Array<Any?>
                    Vector(argsCount, SHIFT, EmptyNode, tail)
                }
                else -> {
                    val empty: TransientVector<E> = EmptyVector.asTransient()

                    args.fold(empty) { tVec, e -> tVec.conj(e) }.persistent()
                }
            }
        }

        private fun tailOffset(count: Int): Int = when {
            count < BF -> 0
            else -> ((count - 1) ushr SHIFT) shl SHIFT
        }

        private tailrec fun <E> newPath(
            isMutable: AtomicBoolean, level: Int, node: Node<E>): Node<E> {
            if (level == 0) return node

            val path = Node<E>(isMutable)
            path.array[0] = node

            return newPath(isMutable, level - SHIFT, path)
        }
    }
}

fun <E> v(): PersistentVector<E> = PersistentVector.EmptyVector

fun <E> v(vararg elements: E): PersistentVector<E> = PersistentVector(*elements)
