package com.github.whyrising.y

interface ILookup<out K, out V> {
    fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V?

    fun valAt(key: @UnsafeVariance K): V?
}
