package com.github.whyrising.y

interface IPersistentVector<out E> {
    fun conj(e: @UnsafeVariance E): IPersistentVector<E>
}