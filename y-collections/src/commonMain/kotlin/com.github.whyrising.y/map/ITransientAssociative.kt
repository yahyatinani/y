package com.github.whyrising.y.map

import com.github.whyrising.y.associative.ILookup
import com.github.whyrising.y.collection.ITransientCollection

interface ITransientAssociative<out K, out V> :
    ITransientCollection<Any?>,
    ILookup<K, V> {

    fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        ITransientAssociative<K, V>
}
