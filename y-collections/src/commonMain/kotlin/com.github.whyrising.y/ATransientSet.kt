package com.github.whyrising.y

abstract class ATransientSet<out K, out V> : ITransientMap<K, V> {

    override fun assoc(
        key: @UnsafeVariance K, value: @UnsafeVariance V): ITransientMap<K, V> {
        TODO("Not yet implemented")
    }

    override fun dissoc(key: @UnsafeVariance K): ITransientMap<K, V> {
        TODO("Not yet implemented")
    }

    override fun persistent(): IPersistentMap<K, V> {
        TODO("Not yet implemented")
    }

    override fun conj(e: Any?): ITransientCollection<Any?> {
        TODO("Not yet implemented")
    }

    override
    fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V? {
        TODO("Not yet implemented")
    }

    override fun valAt(key: @UnsafeVariance K): V? {
        TODO("Not yet implemented")
    }

    override val count: Int
        get() = TODO("Not yet implemented")
}
