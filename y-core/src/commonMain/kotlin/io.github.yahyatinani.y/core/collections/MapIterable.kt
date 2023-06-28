package io.github.yahyatinani.y.core.collections

interface MapIterable<out K, out V> {
  fun keyIterator(): Iterator<K>
  fun valIterator(): Iterator<V>
}
