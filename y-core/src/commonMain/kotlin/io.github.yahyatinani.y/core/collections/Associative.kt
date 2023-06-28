package io.github.yahyatinani.y.core.collections

interface Associative<out K, out V> : ILookup<K, V> {
  fun containsKey(key: @UnsafeVariance K): Boolean

  fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>?

  fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V): Associative<K, V>
}
