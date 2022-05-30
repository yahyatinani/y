package com.github.whyrising.y.core.collections

interface TransientAssociative<out K, out V> :
  ITransientCollection<Any?>,
  ILookup<K, V> {

  fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
    TransientAssociative<K, V>
}
