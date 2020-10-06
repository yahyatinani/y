package com.github.whyrising.y

sealed class PersistentList<out E> :
    IPersistentList<E>, ISeq<E>, List<E>, ConstantCount {

    @Suppress("UNCHECKED_CAST")
    internal class Cons<out E>(
        internal val first: E,
        internal val _rest: IPersistentList<E>
    ) : PersistentList<E>() {
        private var _hashCode: Int = INIT_HASH_CODE
        private val _prime = 31

        override fun first(): E = first

        override fun rest(): ISeq<E> = _rest as ISeq<E>

        override fun cons(e: @UnsafeVariance E): ISeq<E> = Cons(e, this)

        override fun equals(other: Any?): Boolean {
            if (other == null) return false

            if (!(other is ISeq<*> || other is Iterable<*>))
                return false

            val otherIter = (other as Iterable<*>).iterator()
            val thisIter = this.iterator()

            while (thisIter.hasNext() && otherIter.hasNext()) {
                if (thisIter.next() != otherIter.next())
                    return false
            }

            return !otherIter.hasNext()
        }

        override fun hashCode(): Int {
            if (_hashCode != INIT_HASH_CODE) return _hashCode

            _hashCode = fold(Empty.hashCode()) { hashCode: Int, i: E? ->
                _prime * hashCode + i.hashCode()
            }

            return _hashCode
        }

        override fun toString(): String = "(${
            fold("") { acc, e -> "$acc $e" }.trim()
        })"

        override val count: Int = _rest.count + 1

        override fun empty(): IPersistentCollection<E> {
            TODO("Not yet implemented")
        }

        override fun equiv(any: Any?): Boolean {
            TODO("Not yet implemented")
        }

        override fun conj(e: @UnsafeVariance E): IPersistentCollection<E> {
            TODO("Not yet implemented")
        }

        // List implementation
        override val size: Int = count

        override fun contains(element: @UnsafeVariance E): Boolean {
            TODO("Not yet implemented - it needs equiv")
        }

        override
        fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
            TODO("Not yet implemented - it needs equiv")
        }

        override fun get(index: Int): E {
            if (index >= count || index < 0)
                throw IndexOutOfBoundsException("index = $index")

            tailrec fun get(_index: Int, e: E, rest: ISeq<E>): E {
                if (_index == index) return e

                return get(_index.inc(), rest.first(), rest.rest())
            }

            return get(0, this.first, rest())
        }

        override fun indexOf(element: @UnsafeVariance E): Int {
            TODO("Not yet implemented - it needs equiv")
        }

        override fun isEmpty(): Boolean = false

        override fun iterator(): Iterator<E> = SeqIterator(this)

        override fun lastIndexOf(element: @UnsafeVariance E): Int =
            this.toList().lastIndexOf(element)

        override fun listIterator(): ListIterator<E> =
            this.toList().listIterator()

        override fun listIterator(index: Int): ListIterator<E> =
            this.toList().listIterator(index)

        override fun subList(fromIndex: Int, toIndex: Int): List<E> =
            this.toList().subList(fromIndex, toIndex)

        companion object {
            private const val INIT_HASH_CODE = 0
        }
    }

    internal abstract class AEmpty<out E> : PersistentList<E>() {

        override fun toString(): String = "()"

        override fun first(): E =
            throw NoSuchElementException("PersistentList is empty.")

        override fun rest(): ISeq<E> = this

        override fun cons(e: @UnsafeVariance E): ISeq<E> =
            Cons(e, this)

        override val count: Int = 0

        override fun empty(): AEmpty<E> = this

        override fun equiv(any: Any?): Boolean = equals(any)

        override fun conj(e: @UnsafeVariance E): PersistentList<E> =
            Cons(e, Empty)

        // List implementation
        override val size: Int = 0

        override fun contains(element: @UnsafeVariance E): Boolean {
            TODO("Not yet implemented - it doesn't get called")
        }

        override
        fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean =
            elements.isEmpty()

        override fun get(index: Int): E =
            throw IndexOutOfBoundsException("Can't call get on an empty list")

        override fun indexOf(element: @UnsafeVariance E): Int {
            TODO("Not yet implemented - it doesn't get called")
        }

        override fun isEmpty(): Boolean = true

        override fun iterator(): Iterator<E> = object : Iterator<E> {
            override fun hasNext(): Boolean = false

            override fun next(): E = throw NoSuchElementException()
        }

        override fun lastIndexOf(element: @UnsafeVariance E): Int {
            TODO("Not yet implemented - it doesn't get called")
        }

        override fun listIterator(): ListIterator<E> =
            listOf<E>().listIterator()

        override fun listIterator(index: Int): ListIterator<E> =
            listOf<E>().listIterator(index)

        override fun subList(fromIndex: Int, toIndex: Int): List<E> =
            listOf<E>().subList(fromIndex, toIndex)
    }

    internal object Empty : AEmpty<Nothing>()

    companion object {
        operator fun <E> invoke(): PersistentList<E> = Empty

        operator fun <E> invoke(vararg args: E): PersistentList<E> =
            args.foldRight(Empty) { e: E, acc: IPersistentList<E> ->
                Cons(e, acc)
            }
    }
}
