package com.github.whyrising.y

interface IPersistentMap<out K, out V> : Associative<K, V> {
    override fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        IPersistentMap<K, V>

    fun assocNew(key: @UnsafeVariance K, value: @UnsafeVariance V):
        IPersistentMap<K, V>

    fun dissoc(key: @UnsafeVariance K): IPersistentMap<K, V>
}
