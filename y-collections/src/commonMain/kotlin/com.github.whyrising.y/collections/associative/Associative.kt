package com.github.whyrising.y.collections.associative

import com.github.whyrising.y.collections.map.IMapEntry

interface Associative<out K, out V> : ILookup<K, V> {
    fun containsKey(key: @UnsafeVariance K): Boolean

    fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>?

    fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        Associative<K, V>
}
