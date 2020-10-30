package com.github.whyrising.y

interface MapIterable<out K, out V> {

    fun keyIterator(): Iterator<K>

    fun valIterator(): Iterator<V>
}
