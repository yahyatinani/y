package com.github.whyrising.y.core.collections

interface Reversible<out E> {
  fun reverse(): ISeq<E>
}
