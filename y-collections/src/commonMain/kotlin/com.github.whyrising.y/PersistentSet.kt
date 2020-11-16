package com.github.whyrising.y

interface PersistentSet<out E> : IPersistentCollection<E> {
    fun contains(element: @UnsafeVariance E): Boolean
}
