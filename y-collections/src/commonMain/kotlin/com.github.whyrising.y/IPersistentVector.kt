package com.github.whyrising.y

interface IPersistentVector<out E> :
    Associative<Int, E>,
    IPersistentCollection<E>,
    Indexed<E>,
    Sequential {

    fun length(): Int

    override fun conj(e: @UnsafeVariance E): IPersistentVector<E>
}
