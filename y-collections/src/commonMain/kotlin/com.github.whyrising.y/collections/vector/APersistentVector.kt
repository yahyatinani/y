package com.github.whyrising.y.collections.vector

import com.github.whyrising.y.collections.concretions.list.ASeq
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.map.MapEntry
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.EmptyVector
import com.github.whyrising.y.collections.core.IHashEq
import com.github.whyrising.y.collections.core.seq
import com.github.whyrising.y.collections.map.IMapEntry
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.IndexedSeq
import com.github.whyrising.y.collections.seq.Sequential
import com.github.whyrising.y.collections.stack.IPersistentStack
import com.github.whyrising.y.collections.util.HASH_PRIME
import com.github.whyrising.y.collections.util.INIT_HASH_CODE
import com.github.whyrising.y.collections.util.Murmur3
import com.github.whyrising.y.collections.util.compare
import com.github.whyrising.y.collections.util.hasheq
import com.github.whyrising.y.collections.vector.APersistentVector.Seq.Companion.emptySeq

abstract class APersistentVector<out E> :
    IPersistentVector<E>,
    Comparable<IPersistentVector<@UnsafeVariance E>>,
    RandomAccess,
    Reversible<E>,
    IHashEq {

    internal var hashCode: Int = INIT_HASH_CODE
        private set

    internal var hasheq: Int = INIT_HASH_CODE
        private set

    override fun toString(): String {
        var i = 0
        var str = ""
        while (i < count) {
            str += "${nth(i)} "
            i++
        }

        return "[${str.trim()}]"
    }

    override fun seq(): ISeq<E> = when (count) {
        0 -> emptySeq()
        else -> Seq(this)
    }

    override fun reverse(): ISeq<E> = when {
        count > 0 -> RSeq(this, count - 1)
        else -> emptySeq()
    }

    override fun hashCode(): Int {
        var hash = hashCode

        if (hash == INIT_HASH_CODE) {
            hash = 1

            var index = 0
            while (index < count) {
                hash = (31 * hash) + nth(index).hashCode()
                index++
            }
            hashCode = hash
        }

        return hash
    }

    @ExperimentalStdlibApi
    override fun hasheq(): Int {
        var cached = hasheq
        if (cached == INIT_HASH_CODE) {
            cached = 1
            var i = 0
            while (i < count) {
                cached = (HASH_PRIME * cached) + hasheq(nth(i))
                i++
            }

            cached = Murmur3.mixCollHash(cached, i)
            hasheq = cached
        }

        return cached
    }

    private fun compareWith(
        other: Any?,
        areEqual: (e1: E, e2: Any?) -> Boolean
    ): Boolean {
        when {
            other == null -> return false
            this === other -> return true
            other is IPersistentVector<*> -> {
                if (count != other.count) return false

                var i = 0
                while (i < count) {
                    if (!areEqual(nth(i), other.nth(i)))
                        return false
                    i++
                }

                return true
            }
            other is List<*> -> {
                if (other.size != count)
                    return false

                val i1 = iterator()
                val i2 = other.iterator()

                while (i1.hasNext())
                    if (!areEqual(i1.next(), i2.next()))
                        return false

                return true
            }
            else -> {
                if (other !is Sequential) return false

                var seq = seq<E>(other) as ISeq<E>

                var i = 0
                while (i < count) {
                    if (!areEqual(nth(i), seq.first()))
                        return false
                    seq = seq.rest()
                    i++
                }

                if (seq != emptySeq<E>()) return false
            }
        }

        return true
    }

    override fun equals(other: Any?): Boolean = compareWith(other) { e1, e2 ->
        e1 == e2
    }

    override fun equiv(other: Any?): Boolean = compareWith(other) { e1, e2 ->
        com.github.whyrising.y.collections.util.equiv(e1, e2)
    }

    override fun length(): Int = count

    protected fun indexOutOfBounds(index: Int) = index >= count || index < 0

    override fun nth(index: Int, default: @UnsafeVariance E): E = when {
        indexOutOfBounds(index) -> default
        else -> nth(index)
    }

    override fun valAt(key: Int, default: @UnsafeVariance E?): E? = when (key) {
        in 0 until count -> nth(key)
        else -> default
    }

    override fun valAt(key: Int): E? = valAt(key, null)

    override fun containsKey(key: Int): Boolean = key in 0 until count

    override fun entryAt(key: Int): IMapEntry<Int, E>? = when (key) {
        !in 0..count -> null
        else -> MapEntry(key, nth(key))
    }

    override fun assoc(
        key: Int,
        value: @UnsafeVariance E
    ): IPersistentVector<E> = assocN(key, value)

    override fun subvec(start: Int, end: Int): IPersistentVector<E> =
        SubVector(this, start, end)

    protected open fun rangedIterator(start: Int, end: Int): Iterator<E> =
        object : Iterator<E> {
            var i = start

            override fun hasNext(): Boolean = i < end

            override fun next(): E = when {
                hasNext() -> nth(i++)
                else -> throw NoSuchElementException()
            }
        }

    override fun peek(): E? = when {
        count > 0 -> nth(count - 1)
        else -> null
    }

    override fun compareTo(other: IPersistentVector<@UnsafeVariance E>): Int {
        return when {
            count < other.count -> -1
            count > other.count -> 1
            else -> {
                var i = 0
                while (i < count) {
                    val r = compare(nth(i), other.nth(i))

                    if (r != 0) return r

                    i++
                }

                0
            }
        }
    }

    operator fun invoke(index: Int): E = nth(index)

    operator fun plus(element: @UnsafeVariance E): IPersistentVector<E> =
        conj(element)

    // List implementation
    override val size: Int
        get() = count

    override fun contains(element: @UnsafeVariance E): Boolean {
        var seq = seq()
        var i = 0
        while (i < count) {
            if (com.github.whyrising.y.collections.util.equiv(element, nth(i)))
                return true
            seq = seq.rest()
            i++
        }

        return false
    }

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        for (e in elements)
            if (!contains(e))
                return false

        return true
    }

    override fun get(index: Int): E = nth(index)

    override fun indexOf(element: @UnsafeVariance E): Int {
        var i = 0
        while (i < count) {
            if (com.github.whyrising.y.collections.util.equiv(nth(i), element))
                return i
            i++
        }

        return -1
    }

    override fun isEmpty(): Boolean = count == 0

    override fun iterator(): Iterator<E> = object : Iterator<E> {
        var i = 0

        override fun hasNext(): Boolean = i < count

        override fun next(): E = when {
            hasNext() -> nth(i++)
            else -> throw NoSuchElementException()
        }
    }

    override fun lastIndexOf(element: @UnsafeVariance E): Int {
        var i = count - 1
        while (i >= 0) {
            if (com.github.whyrising.y.collections.util.equiv(nth(i), element))
                return i
            i--
        }

        return -1
    }

    override fun listIterator(index: Int): ListIterator<E> =
        object : ListIterator<E> {
            var i = index

            override fun hasNext(): Boolean = i < count

            override fun hasPrevious(): Boolean = i > 0

            override fun next(): E {
                when {
                    i < count -> return nth(i++)
                    else -> throw NoSuchElementException()
                }
            }

            override fun nextIndex(): Int = i

            override fun previous(): E {
                when {
                    i > 0 -> return nth(--i)
                    else -> throw NoSuchElementException()
                }
            }

            override fun previousIndex(): Int = i - 1
        }

    override fun listIterator(): ListIterator<E> = listIterator(0)

    @Suppress("UNCHECKED_CAST")
    override fun subList(fromIndex: Int, toIndex: Int): List<E> =
        SubVector(this, fromIndex, toIndex) as List<E>

    class Seq<out E>(
        private val pv: IPersistentVector<E>,
        override val index: Int = 0
    ) : ASeq<E>(), IndexedSeq {

        override fun first(): E = pv.nth(index)

        override fun rest(): ISeq<E> {
            val i = index + 1

            if (i < pv.count) return Seq(pv, i)

            return emptySeq()
        }

        override fun next(): ISeq<E>? {
            TODO("Not yet implemented")
        }

        override val count: Int
            get() = pv.count - index

        companion object {
            internal fun <E> emptySeq(): ISeq<E> = PersistentList.Empty
        }
    }

    internal class SubVector<out E> private constructor(
        internal val vec: IPersistentVector<E>,
        internal val start: Int,
        internal val end: Int
    ) : APersistentVector<E>() {

        override fun nth(index: Int): E = (start + index).let {
            when {
                index < 0 -> throw IndexOutOfBoundsException(
                    "The index should be >= 0: $index"
                )
                it >= end -> throw IndexOutOfBoundsException()
                else -> return vec.nth(it)
            }
        }

        override
        fun assocN(index: Int, value: @UnsafeVariance E): IPersistentVector<E> =
            (start + index).let {
                when {
                    it > end -> throw IndexOutOfBoundsException(
                        "Index $index is out of bounds."
                    )
                    it == end -> conj(value)
                    else -> SubVector(vec.assocN(it, value), start, end)
                }
            }

        override fun conj(e: @UnsafeVariance E): IPersistentVector<E> =
            SubVector(vec.conj(e), start, end + 1)

        override val count: Int = end - start

        override fun empty(): IPersistentCollection<E> = EmptyVector

        override fun iterator(): Iterator<E> = when (vec) {
            is APersistentVector<E> -> vec.rangedIterator(start, end)
            else -> super.iterator()
        }

        override fun pop(): IPersistentStack<E> = when (count) {
            1 -> EmptyVector
            else -> SubVector(vec, start, end - 1)
        }

        companion object {
            operator fun <E> invoke(
                vec: IPersistentVector<E>,
                start: Int,
                end: Int
            ): IPersistentVector<E> = when {
                start > end -> throw IndexOutOfBoundsException(
                    "Make sure that the start < end: $start < $end!"
                )
                start < 0 -> throw IndexOutOfBoundsException(
                    "Make sure that the start >= 0: $start >= 0!"
                )
                end > vec.count -> throw IndexOutOfBoundsException(
                    "Make sure that the end <= count: $end <= ${vec.count}!"
                )
                start == end -> EmptyVector
                else -> SubVector(vec, start, end)
            }
        }
    }

    internal class RSeq<E>(
        internal val vec: IPersistentVector<E>,
        override var index: Int
    ) : ASeq<E>(), IndexedSeq {

        override fun first(): E = vec.nth(index)

        override fun rest(): ISeq<E> = when {
            index > 0 -> RSeq(vec, index - 1)
            else -> emptySeq()
        }

        override fun next(): ISeq<E>? {
            TODO("Not yet implemented")
        }

        override val count: Int = index + 1
    }
}
