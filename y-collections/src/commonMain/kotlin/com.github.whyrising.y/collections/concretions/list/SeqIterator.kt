package com.github.whyrising.y.collections.concretions.list

import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.seq.ISeq

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
