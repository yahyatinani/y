package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.EmptyVector
import com.github.whyrising.y.collections.core.first
import com.github.whyrising.y.collections.core.l
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.stack.IPersistentStack

class PersistentQueue<out E> private constructor(
    override val count: Int,
    val front: ISeq<E>,
    val back: PersistentVector<E>
) : IPersistentStack<E> {

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

    override fun empty(): IPersistentCollection<E> {
        TODO("Not yet implemented")
    }

    override fun equiv(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun seq(): ISeq<E> {
        TODO("Not yet implemented")
    }

    companion object {
        private val EMPTY_QUEUE = PersistentQueue(0, Empty, EmptyVector)

        operator fun <E> invoke(): PersistentQueue<E> = EMPTY_QUEUE
    }
}
