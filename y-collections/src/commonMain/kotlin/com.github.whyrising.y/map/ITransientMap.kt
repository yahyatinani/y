package com.github.whyrising.y.map

import com.github.whyrising.y.core.ConstantCount

interface ITransientMap<out K, out V> :
    ITransientAssociative<K, V>, ConstantCount {

    override fun assoc(key: @UnsafeVariance K, value: @UnsafeVariance V):
        ITransientMap<K, V>

    fun dissoc(key: @UnsafeVariance K): ITransientMap<K, V>

    override fun persistent(): IPersistentMap<K, V>
}
