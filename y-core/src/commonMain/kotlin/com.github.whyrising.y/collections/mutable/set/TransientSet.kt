package com.github.whyrising.y.collections.mutable.set

import com.github.whyrising.y.collections.InstaCount
import com.github.whyrising.y.collections.mutable.collection.ITransientCollection

interface TransientSet<out E> : InstaCount, ITransientCollection<E> {
    fun disjoin(key: @UnsafeVariance E): TransientSet<E>

    @Suppress("UNCHECKED_CAST")
    fun contains(key: @UnsafeVariance E): Boolean

    operator fun get(key: @UnsafeVariance E): E?
}
