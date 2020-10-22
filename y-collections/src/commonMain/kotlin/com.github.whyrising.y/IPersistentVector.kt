package com.github.whyrising.y

interface IPersistentVector<out E> :
    Associative<Int, E>,
    IPersistentCollection<E>,
    Indexed<E>,
    Sequential {

    fun length(): Int

    fun assocN(index: Int, value: @UnsafeVariance E): IPersistentVector<E>

    override fun conj(e: @UnsafeVariance E): IPersistentVector<E>
}
