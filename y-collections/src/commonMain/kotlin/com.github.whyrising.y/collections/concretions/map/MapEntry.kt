package com.github.whyrising.y.collections.concretions.map

import com.github.whyrising.y.collections.map.AMapEntry

class MapEntry<out K, out V>(
    override val key: K,
    override val value: V
) : AMapEntry<K, V>()
