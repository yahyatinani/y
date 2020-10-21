package com.github.whyrising.y

interface IPersistentVector<out E> : Indexed<E>, Sequential {
    fun length(): Int

    fun conj(e: @UnsafeVariance E): IPersistentVector<E>
}
