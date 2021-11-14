package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.ASeq
import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.EmptyVector
import com.github.whyrising.y.collections.core.IHashEq
import com.github.whyrising.y.collections.core.InstaCount
import com.github.whyrising.y.collections.core.first
import com.github.whyrising.y.collections.core.l
import com.github.whyrising.y.collections.list.IPersistentList
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.util.count
import com.github.whyrising.y.collections.util.equiv
import kotlinx.serialization.Transient

class PersistentQueue<out E> private constructor(
    override val count: Int,
    val front: ISeq<E>,
    val back: PersistentVector<E>
) : IPersistentList<E>, Collection<E>, InstaCount, IHashEq {
    @Transient
    private var _hash: Int = 0

    override fun equiv(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasheq(): Int {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int {
        if (_hash == 0) {
            var hash = 1
            var s: ISeq<E>? = seq()
            while (s != null && s !is Empty) {
                hash = 31 * hash + (s.first()?.hashCode() ?: 0)
                s = s.next()
            }
            _hash = hash
        }

        return _hash
    }

    override fun peek(): E? = first<E>(front)

    override fun pop(): PersistentQueue<E> = when (front) {
        is Empty -> EMPTY_QUEUE
        else -> {
            val newFront = front.rest()
            val c = count - 1
            when (newFront) {
                is Empty -> PersistentQueue(c, back.seq(), EmptyVector)
                else -> PersistentQueue(c, newFront, back)
            }
        }
    }

    override fun conj(e: @UnsafeVariance E): PersistentQueue<E> = when (front) {
        is Empty -> PersistentQueue(count + 1, l(e), EmptyVector)
        else -> PersistentQueue(count + 1, front, back.conj(e))
    }

    override fun empty(): IPersistentCollection<E> = EMPTY_QUEUE

    override fun seq(): ISeq<E> = when (front) {
        is Empty -> Empty
        else -> Seq(front, back.seq())
    }

    // Collection implementation

    override val size: Int
        get() = count

    override fun contains(element: @UnsafeVariance E): Boolean {
        var s: ISeq<E>? = seq()

        while (s != null && s !is Empty) {
            if (equiv(s.first(), element))
                return true
            s = s.next()
        }

        return false
    }

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        for (e in elements)
            if (!contains(e)) return false
        return true
    }

    override fun isEmpty(): Boolean = count == 0

    override fun iterator(): Iterator<E> {
        TODO("Not yet implemented")
    }

    class Seq<E>(
        private val front: ISeq<E>,
        private val back: ISeq<E>
    ) : ASeq<E>() {
        override val count: Int
            get() = count(front) + count(back)

        override fun first(): E = front.first()

        override fun next(): ISeq<E>? = when (front) {
            is Empty -> null
            else -> {
                val nextF = front.next()
                when (back) {
                    is Empty -> nextF
                    else -> when (nextF) {
                        null -> back
                        else -> Seq(nextF, back)
                    }
                }
            }
        }
    }

    companion object {
        private val EMPTY_QUEUE = PersistentQueue(0, Empty, EmptyVector)
        operator fun <E> invoke(): PersistentQueue<E> = EMPTY_QUEUE
    }
}
