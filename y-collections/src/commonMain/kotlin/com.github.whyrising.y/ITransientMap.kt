package com.github.whyrising.y

interface ITransientMap<out K, out V> : ITransientCollection<Any?>,
    ConstantCount {

    fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        ITransientMap<K, V>

    fun dissoc(key: @UnsafeVariance K): ITransientMap<K, V>

    override fun persistent(): IPersistentMap<K, V>
}
