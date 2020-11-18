package com.github.whyrising.y.collection

import com.github.whyrising.y.seq.IPersistentCollection

interface ITransientCollection<out E> {
    fun conj(e: @UnsafeVariance E): ITransientCollection<E>

    fun persistent(): IPersistentCollection<E>
}
