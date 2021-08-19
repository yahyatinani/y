package com.github.whyrising.y.collections.seq

interface IPersistentCollection<out E> : Seqable<E> {
    val count: Int

    fun empty(): IPersistentCollection<E>

    fun equiv(other: Any?): Boolean

    fun conj(e: @UnsafeVariance E): IPersistentCollection<E>
}
