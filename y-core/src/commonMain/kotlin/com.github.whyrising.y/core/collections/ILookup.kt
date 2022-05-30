package com.github.whyrising.y.core.collections

interface ILookup<out K, out V> {
  fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V?

  fun valAt(key: @UnsafeVariance K): V?
}
