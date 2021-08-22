package com.github.whyrising.y.collections.map

interface MapIterable<out K, out V> {

    fun keyIterator(): Iterator<K>

    fun valIterator(): Iterator<V>
}
