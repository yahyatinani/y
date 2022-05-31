package com.github.whyrising.y.core.collections

class MapEntry<out K, out V>(override val key: K, override val value: V) :
  AMapEntry<K, V>()
