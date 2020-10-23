package com.github.whyrising.y

interface Associative<K, out V> : ILookup<Int, V> {
    fun containsKey(key: K): Boolean

    fun entryAt(key: K): IMapEntry<Int, V>?

    fun assoc(index: Int, value: @UnsafeVariance V): IPersistentVector<V>
}
