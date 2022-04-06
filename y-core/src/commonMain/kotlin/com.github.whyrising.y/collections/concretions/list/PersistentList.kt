package com.github.whyrising.y.collections.concretions.list

import com.github.whyrising.y.collections.InstaCount
import com.github.whyrising.y.collections.concretions.serialization.PersistentListSerializer
import com.github.whyrising.y.collections.list.IPersistentList
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.stack.IPersistentStack
import com.github.whyrising.y.util.Murmur3
import kotlinx.serialization.Serializable

@Serializable(with = PersistentListSerializer::class)
sealed class PersistentList<out E> : ASeq<E>(), IPersistentList<E>, InstaCount {

    override fun conj(e: @UnsafeVariance E): PersistentList<E> = Cons(e, this)

    override fun cons(e: @UnsafeVariance E): ISeq<E> = Cons(e, this)

    @Suppress("UNCHECKED_CAST")
    internal class Cons<out E>(
        internal val first: E,
        internal val rest: IPersistentList<E>
    ) : PersistentList<E>() {

        override fun first(): E = first

        override fun next(): ISeq<E>? = when (count) {
            1 -> null
            else -> rest as ISeq<E>?
        }

        override val count: Int = rest.count + 1

        override fun empty(): IPersistentCollection<E> = Empty

        override fun peek(): E = first

        override fun pop(): IPersistentStack<E> = rest
    }

    internal abstract class AEmpty<out E> : PersistentList<E>() {

        override fun toString(): String = "()"

        override fun hasheq(): Int = HASH_EQ

        override fun first(): E = throw NoSuchElementException(
            "Calling first() on empty PersistentList."
        )

        override fun rest(): ISeq<E> = this

        override fun next(): ISeq<E>? = null

        override val count: Int = 0

        override fun empty(): AEmpty<E> = this

        override fun equiv(other: Any?): Boolean = equals(other)

        override fun peek(): E? = null

        override fun pop(): IPersistentStack<E> = this

        // List implementation
        override val size: Int = 0

        override
        fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean =
            elements.isEmpty()

        override fun get(index: Int): E =
            throw IndexOutOfBoundsException("Can't call get on an empty list")

        override fun isEmpty(): Boolean = true

        override fun iterator(): Iterator<E> = object : Iterator<E> {
            override fun hasNext(): Boolean = false

            override fun next(): E = throw NoSuchElementException()
        }

        override fun listIterator(): ListIterator<E> =
            listOf<E>().listIterator()

        override fun listIterator(index: Int): ListIterator<E> =
            listOf<E>().listIterator(index)

        override fun subList(fromIndex: Int, toIndex: Int): List<E> =
            listOf<E>().subList(fromIndex, toIndex)

        override fun contains(element: @UnsafeVariance E): Boolean = false

        override fun indexOf(element: @UnsafeVariance E): Int = -1

        override fun lastIndexOf(element: @UnsafeVariance E): Int = -1

        companion object {
            private val HASH_EQ = Murmur3.hashOrdered(emptyList<Nothing>())
        }
    }

    internal object Empty : AEmpty<Nothing>()

    companion object {
        internal operator fun <E> invoke(): PersistentList<E> = Empty

        internal operator fun <E> invoke(vararg args: E): PersistentList<E> =
            args.foldRight(Empty) { e: E, acc: IPersistentList<E> ->
                Cons(e, acc)
            }

        internal fun <E> create(list: List<E>): PersistentList<E> {
            val listIterator = list.listIterator(list.size)

            var l: PersistentList<E> = Empty
            while (listIterator.hasPrevious())
                l = l.conj(listIterator.previous())

            return l
        }
    }
}
