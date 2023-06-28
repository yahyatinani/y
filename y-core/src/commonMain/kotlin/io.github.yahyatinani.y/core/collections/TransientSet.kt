package io.github.yahyatinani.y.core.collections

interface TransientSet<out E> : InstaCount, ITransientCollection<E> {
  fun disjoin(key: @UnsafeVariance E): TransientSet<E>

  @Suppress("UNCHECKED_CAST")
  fun contains(key: @UnsafeVariance E): Boolean

  operator fun get(key: @UnsafeVariance E): E?
}
