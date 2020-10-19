package com.github.whyrising.y

interface ITransientCollection<out E> {
    fun conj(e: @UnsafeVariance E): ITransientCollection<E>

    fun persistent(): PersistentVector<E> //TODO: return IPersistentCollection
}
