package com.github.whyrising.y

interface ITransientAssociative2<out K, out V> : ITransientAssociative<K, V> {

    fun containsKey(key: @UnsafeVariance K): Boolean

    fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>?
}
