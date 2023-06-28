package io.github.yahyatinani.y.core.collections

interface ITransientCollection<out E> {
  fun conj(e: @UnsafeVariance E): ITransientCollection<E>

  fun persistent(): IPersistentCollection<E>
}
