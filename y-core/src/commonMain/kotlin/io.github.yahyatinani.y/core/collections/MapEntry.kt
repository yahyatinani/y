package io.github.yahyatinani.y.core.collections

class MapEntry<out K, out V>(override val key: K, override val value: V) :
  io.github.yahyatinani.y.core.collections.AMapEntry<K, V>()
