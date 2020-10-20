package com.github.whyrising.y

abstract class APersistentVector<out E>
    : IPersistentVector<E>, List<E>, Seqable<E> {
    internal var _hashCode: Int = INIT_HASH_CODE

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
        if (count == 0) return Seq.emptySeq()

        return Seq(this)
    }

    override fun hashCode(): Int {
        var hash = _hashCode

        if (hash != INIT_HASH_CODE) return hash

        var index = 0
        while (index < count) {
            hash = 31 * hash + nth(index).hashCode()

            index++
        }
        _hashCode = hash

        return _hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false

        if (other is PersistentVector<*>) {
            if (count != other.count) return false

            var i = 0
            while (i < count) {
                if (nth(i) != other.nth(i))
                    return false
                i++
            }

            return true
        } else if (other is List<*>) {
//            if (count != other.size)
//            val i1 = (this as List<E>).iterator()
//            val i2 = other.iterator()
//
//            while (i1.hasNext())
//                if (i1.next() != i2.next())
//                    return false

            return false
        }
        TODO()
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
