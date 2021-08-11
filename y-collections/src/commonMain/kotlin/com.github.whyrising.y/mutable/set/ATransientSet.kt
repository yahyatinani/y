package com.github.whyrising.y.mutable.set

import com.github.whyrising.y.mutable.map.TransientMap
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update

abstract class ATransientSet<out E>(
    transientMap: TransientMap<E, E>
) : TransientSet<E> {
    internal val _transientMap =
        atomic<TransientMap<@UnsafeVariance E, @UnsafeVariance E>>(transientMap)

    override val count: Int
        get() = _transientMap.value.count

    override fun disjoin(key: @UnsafeVariance E): TransientSet<E> {
        _transientMap.update {
            val transientMap = it.dissoc(key)
            when {
                transientMap != it -> transientMap
                else -> it
            }
        }

        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun contains(key: @UnsafeVariance E): Boolean =
        NOT_FOUND != _transientMap.value.valAt(key, NOT_FOUND as E)

    override
    operator fun get(key: @UnsafeVariance E): E? =
        _transientMap.value.valAt(key)

    override fun conj(e: @UnsafeVariance E): TransientSet<E> {
        _transientMap.update {
            val transientMap = it.assoc(e, e)
            when {
                transientMap != it -> transientMap
                else -> it
            }
        }
        return this
    }

    operator
    fun invoke(key: @UnsafeVariance E, default: @UnsafeVariance E): E? =
        _transientMap.value.valAt(key, default)

    operator
    fun invoke(key: @UnsafeVariance E): E? =
        _transientMap.value.valAt(key, null)

    companion

    object {
        val NOT_FOUND = Any()
    }
}
