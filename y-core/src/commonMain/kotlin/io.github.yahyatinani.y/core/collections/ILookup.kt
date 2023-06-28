package io.github.yahyatinani.y.core.collections

interface ILookup<out K, out V> {
  fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V?

  fun valAt(key: @UnsafeVariance K): V?
}
