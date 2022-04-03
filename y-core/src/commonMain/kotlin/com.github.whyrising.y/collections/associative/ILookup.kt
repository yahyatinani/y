package com.github.whyrising.y.collections.associative

interface ILookup<out K, out V> {
    fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V?

    fun valAt(key: @UnsafeVariance K): V?
}
