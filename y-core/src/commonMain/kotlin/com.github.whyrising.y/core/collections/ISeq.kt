package com.github.whyrising.y.core.collections

interface ISeq<out E> : IPersistentCollection<E> {
  fun first(): E

  fun rest(): ISeq<E>

  fun next(): ISeq<E>?

  fun cons(e: @UnsafeVariance E): ISeq<E>
}
