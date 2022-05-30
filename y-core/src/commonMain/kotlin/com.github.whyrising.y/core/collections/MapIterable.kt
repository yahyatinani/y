package com.github.whyrising.y.core.collections

interface MapIterable<out K, out V> {
  fun keyIterator(): Iterator<K>
  fun valIterator(): Iterator<V>
}
