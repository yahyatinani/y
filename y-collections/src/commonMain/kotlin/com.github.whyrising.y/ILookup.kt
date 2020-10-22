package com.github.whyrising.y

interface ILookup<K, out V> {
    fun valAt(key: K, default: @UnsafeVariance V): V
}
