package com.github.whyrising.y.set

import com.github.whyrising.y.collection.ITransientCollection
import com.github.whyrising.y.core.ConstantCount

interface TransientSet<out E> : ConstantCount, ITransientCollection<E> {
    fun disjoin(key: @UnsafeVariance E): TransientSet<E>

    @Suppress("UNCHECKED_CAST")
    fun contains(key: @UnsafeVariance E): Boolean

    operator fun get(key: @UnsafeVariance E): E?
}
