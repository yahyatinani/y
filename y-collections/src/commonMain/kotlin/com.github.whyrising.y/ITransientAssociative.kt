package com.github.whyrising.y

interface ITransientAssociative<out K, out V> :
    ITransientCollection<Any?>,
    ILookup<K, V> {

    fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        ITransientAssociative<K, V>
}
