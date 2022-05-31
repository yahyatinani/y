package com.github.whyrising.y.core.collections

interface Seqable<out E> {
  fun seq(): ISeq<E>
}
