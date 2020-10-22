package com.github.whyrising.y

interface Associative<K, out V> : ILookup<Int, V> {
    fun containsKey(key: K): Boolean
}
