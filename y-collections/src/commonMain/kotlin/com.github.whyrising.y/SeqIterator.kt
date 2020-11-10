package com.github.whyrising.y

data class SeqIterator<out E>(
    internal var next: ISeq<@UnsafeVariance E>
) : Iterator<E> {

    private var currentSeq: ISeq<@UnsafeVariance E> = emptySeq()

    override fun hasNext(): Boolean {

        if (currentSeq === next)
            next = currentSeq.rest()

        return next !is PersistentList.Empty
    }

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()

        currentSeq = next

        return next.first()
    }
}
