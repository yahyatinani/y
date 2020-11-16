package com.github.whyrising.y

interface TransientSet<out E> : ConstantCount {
    fun disjoin(key: @UnsafeVariance E): TransientSet<E>

    @Suppress("UNCHECKED_CAST")
    fun contains(key: @UnsafeVariance E): Boolean

    operator fun get(key: @UnsafeVariance E): E?
}
