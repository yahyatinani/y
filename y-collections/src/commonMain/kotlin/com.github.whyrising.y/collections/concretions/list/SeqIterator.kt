package com.github.whyrising.y.collections.concretions.list

import com.github.whyrising.y.collections.seq.ISeq

class SeqIterator<out E>(next: ISeq<@UnsafeVariance E>?) : Iterator<E> {
    internal var next: ISeq<@UnsafeVariance E>? = when (next) {
        is PersistentList.Empty -> null
        else -> next
    }

    private var currentSeq: ISeq<@UnsafeVariance E>? = null

    override fun hasNext(): Boolean {
        if (currentSeq === next)
            next = currentSeq?.next()

        return next != null
    }

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()

        currentSeq = next

        return next!!.first()
    }
}
