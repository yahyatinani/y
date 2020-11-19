package com.github.whyrising.y.mutable.map

import com.github.whyrising.y.associative.ILookup
import com.github.whyrising.y.mutable.collection.ITransientCollection

interface TransientAssociative<out K, out V> :
    ITransientCollection<Any?>,
    ILookup<K, V> {

    fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        TransientAssociative<K, V>
}
