package com.github.whyrising.y.collections.concretions.list

import com.github.whyrising.y.collections.IHashEq
import com.github.whyrising.y.collections.InstaCount
import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.Sequential
import com.github.whyrising.y.seq
import com.github.whyrising.y.util.HASH_PRIME
import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.util.equiv
import com.github.whyrising.y.util.nth
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic

abstract class ASeq<out E> : ISeq<E>, List<E>, Sequential, IHashEq {
    internal var hashCode: AtomicInt = atomic(0)
        private set

    internal var hasheq: Int = 0
        private set

    override
    fun toString(): String = "(${fold("") { acc, e -> "$acc $e" }.trim()})"

    private fun compareWith(
        other: Any?,
        areEqual: (e1: E, e2: Any?) -> Boolean
    ): Boolean {
        when {
            this === other -> return true
            other !is List<*> && other !is Sequential -> return false
            else -> {
                var thisSeq: ISeq<E>? = seq()
                var otherSeq: ISeq<E>? = seq(other)
                while (thisSeq !is Empty && thisSeq != null) {
                    if (
                        otherSeq == null ||
                        !areEqual(thisSeq.first(), otherSeq.first())
                    ) return false

                    thisSeq = thisSeq.next()
                    otherSeq = otherSeq.next()
                }

                return otherSeq == null
            }
        }
    }

    override fun equals(other: Any?): Boolean = compareWith(other) { e1, e2 ->
        e1 == e2
    }

    override fun equiv(other: Any?): Boolean = compareWith(other) { e1, e2 ->
        equiv(e1, e2)
    }

    override fun hashCode(): Int {
        val cached = hashCode.value
        if (cached == 0) {
            var newVal = 1
            var seq = seq()
            while (seq.count > 0) {
                newVal = (HASH_PRIME * newVal) + seq.first().hashCode()
                seq = seq.rest()
            }
            if (hashCode.compareAndSet(cached, newVal))
                return newVal
        }
        return cached
    }

    override fun hasheq(): Int {
        if (hasheq == 0)
            hasheq = Murmur3.hashOrdered(this)

        return hasheq
    }

    override fun seq(): ISeq<E> = this

    override fun rest(): ISeq<E> = next() ?: Empty

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
                if (s is InstaCount)
                    return i + s.count
                s = s.rest()
                i++
            }
            return i
        }

    // List Implementation
    override val size: Int
        get() = count

    override fun contains(element: @UnsafeVariance E): Boolean {
        val iter = iterator()

        while (iter.hasNext())
            if (equiv(iter.next(), element)) return true

        return false
    }

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        for (e in elements)
            if (!contains(e)) return false
        return true
    }

    override fun get(index: Int): E = nth(this, index)

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
