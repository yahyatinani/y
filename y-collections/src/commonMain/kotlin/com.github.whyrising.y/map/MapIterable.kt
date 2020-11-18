package com.github.whyrising.y.map

interface MapIterable<out K, out V> {

    fun keyIterator(): Iterator<K>

    fun valIterator(): Iterator<V>
}
