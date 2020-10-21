package com.github.whyrising.y

import com.github.whyrising.y.APersistentVector.Seq.Companion.emptySeq

abstract class APersistentVector<out E>
    : IPersistentVector<E>, List<E>, Seqable<E> {
    private var _hashCode: Int = INIT_HASH_CODE

    override fun toString(): String {
        var i = 0
        var str = ""
        while (i < count) {
            str += "${nth(i)} "
            i++
        }

        return "[${str.trim()}]"
    }

    override fun seq(): ISeq<E> {
        if (count == 0) return emptySeq()

        return Seq(this)
    }

    override fun hashCode(): Int {
        var hash = _hashCode

        if (hash != INIT_HASH_CODE) return hash

        var index = 0
        hash = 1
        while (index < count) {
            hash = 31 * hash + nth(index).hashCode()

            index++
        }
        _hashCode = hash

        return hash
    }

    private fun compareWith(
        other: Any?,
        areEqual: (e1: E, e2: Any?) -> Boolean
    ): Boolean {
        when (other) {
            null -> return false
            other.hashCode() != hashCode() -> return false
            is IPersistentVector<*> -> {
                if (count != other.count) return false

                var i = 0
                while (i < count) {
                    if (!areEqual(nth(i), other.nth(i)))
                        return false
                    i++
                }

                return true
            }
            is List<*> -> {
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

                var seq: ISeq<E> = toSeq(other)

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
        equiv(e1, e2)
    }

    override fun length(): Int = count

    protected fun indexOutOfBounds(index: Int) = index >= count || index < 0

    override fun nth(index: Int, default: @UnsafeVariance E): E = when {
        indexOutOfBounds(index) -> default
        else -> nth(index)
    }

    // List implementation
    override val size: Int
        get() = count

    override fun contains(element: @UnsafeVariance E): Boolean {
        TODO("Not yet implemented - needs seq()")
    }

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): E = nth(index)

    override fun indexOf(element: @UnsafeVariance E): Int {
        var i = 0
        while (i < count) {
            if (equiv(nth(i), element))
                return i
            i++
        }

        return -1
    }

    override fun isEmpty(): Boolean = count == 0

    override fun iterator(): Iterator<E> {
        TODO("not needed yet, until another class inherit this class")
    }

    override fun lastIndexOf(element: @UnsafeVariance E): Int {
        var i = count - 1
        while (i >= 0) {
            if (equiv(nth(i), element))
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

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        TODO("Not yet implemented - needs Subvec")
    }

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

        override val count: Int
            get() = pv.count - index

        companion object {
            internal fun <E> emptySeq(): ISeq<E> = PersistentList.Empty
        }
    }
}
