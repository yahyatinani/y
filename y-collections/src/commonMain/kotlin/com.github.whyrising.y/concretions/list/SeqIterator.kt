package com.github.whyrising.y.concretions.list

import com.github.whyrising.y.concretions.list.PersistentList.Empty
import com.github.whyrising.y.seq.ISeq

data class SeqIterator<out E>(
    internal var next: ISeq<@UnsafeVariance E>
) : Iterator<E> {

    private var currentSeq: ISeq<@UnsafeVariance E> = Empty

    override fun hasNext(): Boolean {

        if (currentSeq === next)
            next = currentSeq.rest()

        return !(next === Empty)
    }

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()

        currentSeq = next

        return next.first()
    }
}
