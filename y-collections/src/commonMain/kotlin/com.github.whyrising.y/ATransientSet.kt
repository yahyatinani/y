package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

abstract class ATransientSet<out E>(
    private
    val tranMap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
) : TransientSet<E> {
    override val count: Int
        get() = tranMap.value.count

    override fun disjoin(key: @UnsafeVariance E): TransientSet<E> {
        val m = tranMap.value.dissoc(key)
        if (tranMap.value != m) tranMap.value = m
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun contains(key: @UnsafeVariance E): Boolean =
        NOT_FOUND != tranMap.value.valAt(key, NOT_FOUND as E)

    override
    operator fun get(key: @UnsafeVariance E): E? = tranMap.value.valAt(key)

    override fun conj(e: @UnsafeVariance E): TransientSet<E> {
        val m = tranMap.value.assoc(e, e)

        if (m != tranMap.value) tranMap.value = m

        return this
    }

    operator
    fun invoke(key: @UnsafeVariance E, default: @UnsafeVariance E): E? =
        tranMap.value.valAt(key, default)

    operator
    fun invoke(key: @UnsafeVariance E): E? = tranMap.value.valAt(key, null)

    companion

    object {
        val NOT_FOUND = Any()
    }
}
