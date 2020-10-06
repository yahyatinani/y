package com.github.whyrising.y

interface IPersistentCollection<out E> {
    val count: Int

    fun empty(): IPersistentCollection<E>

    fun equiv(other: Any?): Boolean

    fun conj(e: @UnsafeVariance E): IPersistentCollection<E>
}