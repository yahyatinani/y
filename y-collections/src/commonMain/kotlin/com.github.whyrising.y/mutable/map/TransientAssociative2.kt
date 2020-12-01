package com.github.whyrising.y.mutable.map

import com.github.whyrising.y.map.IMapEntry

interface TransientAssociative2<out K, out V> : TransientAssociative<K, V> {

    fun containsKey(key: @UnsafeVariance K): Boolean

    fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>?
}
