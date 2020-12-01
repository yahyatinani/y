package com.github.whyrising.y.mutable.set

import com.github.whyrising.y.core.InstaCount
import com.github.whyrising.y.mutable.collection.ITransientCollection

interface TransientSet<out E> : InstaCount, ITransientCollection<E> {
    fun disjoin(key: @UnsafeVariance E): TransientSet<E>

    @Suppress("UNCHECKED_CAST")
    fun contains(key: @UnsafeVariance E): Boolean

    operator fun get(key: @UnsafeVariance E): E?
}
