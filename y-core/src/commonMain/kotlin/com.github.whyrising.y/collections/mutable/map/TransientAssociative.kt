package com.github.whyrising.y.collections.mutable.map

import com.github.whyrising.y.collections.associative.ILookup
import com.github.whyrising.y.collections.mutable.collection.ITransientCollection

interface TransientAssociative<out K, out V> :
    ITransientCollection<Any?>,
    ILookup<K, V> {

    fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        TransientAssociative<K, V>
}
