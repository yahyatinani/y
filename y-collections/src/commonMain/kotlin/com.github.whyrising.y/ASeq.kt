package com.github.whyrising.y

import com.github.whyrising.y.PersistentList.Empty

abstract class ASeq<out E> : ISeq<E>, List<E>, Sequential {
    private var _hashCode: Int = INIT_HASH_CODE

    override fun toString(): String = "(${
        fold("") { acc, e -> "$acc $e" }.trim()
    })"

    private fun compareWith(
        other: Any?,
        areEqual: (e1: E, e2: Any?) -> Boolean
    ): Boolean {
        //TODO : Refactor after implementing the LazySeq
        when (other) {
            null -> return false
            hashCode() != other.hashCode() -> return false
            is List<*> -> {
                if (count != other.size)
                    return false

                val otherIter = (other as Iterable<*>).iterator()
                val thisIter = this.iterator()

                while (thisIter.hasNext() && otherIter.hasNext()) {
                    if (!areEqual(thisIter.next(), otherIter.next()))
                        return false
                }

                return !otherIter.hasNext()
            }
            is Sequential -> {
                var seq = seq()
                var otherSeq = toSeq<E>(other) as ISeq<E>

                var i = 0
                while (i < count) {
                    if (!areEqual(seq.first(), otherSeq.first()))
                        return false

                    i++
                    seq = seq.rest()
                    otherSeq = otherSeq.rest()
                }

                return otherSeq == empty()
            }
            else -> return false
        }
    }

    override fun equals(other: Any?): Boolean = compareWith(other) { e1, e2 ->
        e1 == e2
    }

    override fun equiv(other: Any?): Boolean = compareWith(other) { e1, e2 ->
        equiv(e1, e2)
    }

    override fun hashCode(): Int {
        if (_hashCode != INIT_HASH_CODE) return _hashCode

        _hashCode = fold(Empty.hashCode()) { hashCode: Int, i: E? ->
            HASH_PRIME * hashCode + i.hashCode()
        }

        return _hashCode
    }

    override fun seq(): ISeq<E> = this

    override fun empty(): IPersistentCollection<E> = Empty

    override fun cons(e: @UnsafeVariance E): ISeq<E> = Cons(e, this)

    override fun conj(e: @UnsafeVariance E): IPersistentCollection<E> = cons(e)

    operator fun plus(element: @UnsafeVariance E): IPersistentCollection<E> =
        conj(element)

    override val count: Int
        get() {
            var i = 1
            var s = rest()
            while (s !is Empty) {
                if (s is ConstantCount)
                    return i + s.count
                s = s.rest()
                i++
            }
            return i
        }

    //List Implementation
    override val size: Int
        get() = count

    override fun contains(element: @UnsafeVariance E): Boolean {
        val iter = iterator()

        while (iter.hasNext())
            if (equiv(iter.next(), element)) return true

        return false
    }

    override
    fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean =
        elements.fold(true) { acc: Boolean, e: E ->
            acc && contains(e)
        }

    override fun get(index: Int): E {
        if (index >= count || index < 0)
            throw IndexOutOfBoundsException("index = $index")

        tailrec fun get(_index: Int, e: E, rest: ISeq<E>): E {
            if (_index == index) return e

            return get(_index.inc(), rest.first(), rest.rest())
        }

        return get(0, first(), rest())
    }

    override fun indexOf(element: @UnsafeVariance E): Int {
        for ((index, n) in this.withIndex())
            if (equiv(n, element)) return index

        return -1
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
}
