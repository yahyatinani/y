package com.github.whyrising.y

abstract class ATransientMap<out K, out V> {

    internal abstract fun assertMutable()

    internal abstract fun doPersistent(): PersistentArrayMap.ArrayMap<K, V>

    fun persistent(): IPersistentMap<K, V> {
        assertMutable()

        return doPersistent()
    }
}
