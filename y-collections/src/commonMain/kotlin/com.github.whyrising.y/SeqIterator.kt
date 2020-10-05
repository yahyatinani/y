package com.github.whyrising.y

import com.github.whyrising.y.PersistentList.Empty

data class SeqIterator<out E>(
    internal var next: ISeq<@UnsafeVariance E>
) : Iterator<E> {

    private var currentSeq: ISeq<@UnsafeVariance E> = Empty

    override fun hasNext(): Boolean {

        if (currentSeq === next)
            next = currentSeq.rest()

        return next !is Empty
    }

    override fun next(): E {

        if (!hasNext()) throw NoSuchElementException()

        currentSeq = next

        return next.first()
    }
}
