package com.github.whyrising.y.collections.mutable.collection

import com.github.whyrising.y.collections.seq.IPersistentCollection

interface ITransientCollection<out E> {
    fun conj(e: @UnsafeVariance E): ITransientCollection<E>

    fun persistent(): IPersistentCollection<E>
}
