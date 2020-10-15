package com.github.whyrising.y

interface IPersistentVector<out E> : Indexed<E> {
    fun conj(e: @UnsafeVariance E): IPersistentVector<E>
}
