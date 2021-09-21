package com.github.whyrising.y.collections.concretions.list

import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.core.seq
import com.github.whyrising.y.collections.seq.ISeq

class SeqIterator<out E>(next: ISeq<@UnsafeVariance E>?) : Iterator<E> {
    private var isFresh = true

    internal var next: ISeq<@UnsafeVariance E>? = when (next) {
        Empty -> null
        else -> next
    }

    private var currentSeq: ISeq<@UnsafeVariance E>? = null

    override fun hasNext(): Boolean {
        when {
            isFresh -> {
                isFresh = false

                next = seq(next)
            }
            currentSeq === next -> {
                next = currentSeq?.next()
            }
        }

        return next != null
    }

    override fun next(): E {
        if (!hasNext()) throw NoSuchElementException()

        currentSeq = next

        return next!!.first()
    }
}

fun <E> next(x: Any?): ISeq<E>? {
    if (x is ISeq<*>) return x.next() as ISeq<E>?

    val seq: ISeq<E> = seq(x) ?: return null

    return seq.next()
}