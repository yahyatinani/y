package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.collections.PersistentList.Empty
import com.github.whyrising.y.core.seq

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
