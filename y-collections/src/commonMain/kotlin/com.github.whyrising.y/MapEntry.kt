package com.github.whyrising.y

class MapEntry<out K, out V>(
    override val key: K,
    override val value: V
) : AMapEntry<K, V>()
