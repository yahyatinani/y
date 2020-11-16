package com.github.whyrising.y

interface PersistentSet<out E> : IPersistentCollection<E> {
    fun contains(element: @UnsafeVariance E): Boolean

    fun disjoin(e: @UnsafeVariance E): PersistentSet<E>

    operator fun get(key: @UnsafeVariance E): E?
}
