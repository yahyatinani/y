package com.github.whyrising.y.mutable.map

import com.github.whyrising.y.core.InstaCount
import com.github.whyrising.y.map.IPersistentMap

interface TransientMap<out K, out V> :
    TransientAssociative<K, V>, InstaCount {

    override fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        TransientMap<K, V>

    fun dissoc(key: @UnsafeVariance K): TransientMap<K, V>

    override fun persistent(): IPersistentMap<K, V>
}
