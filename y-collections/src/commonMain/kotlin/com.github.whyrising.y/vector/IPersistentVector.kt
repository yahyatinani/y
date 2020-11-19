package com.github.whyrising.y.vector

import com.github.whyrising.y.associative.Associative
import com.github.whyrising.y.seq.IPersistentCollection
import com.github.whyrising.y.seq.Sequential
import com.github.whyrising.y.stack.IPersistentStack

interface IPersistentVector<out E> :
    Associative<Int, E>,
    IPersistentCollection<E>,
    IPersistentStack<E>,
    Indexed<E>,
    Sequential {

    fun length(): Int

    fun assocN(index: Int, value: @UnsafeVariance E): IPersistentVector<E>

    override fun conj(e: @UnsafeVariance E): IPersistentVector<E>

    fun subvec(start: Int, end: Int): IPersistentVector<E>
}
