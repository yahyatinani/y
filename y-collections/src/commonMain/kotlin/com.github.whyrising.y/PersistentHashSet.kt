package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) {
    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal class TransientHashSet<out E>(
        internal
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : ConstantCount {

        fun disjoin(key: @UnsafeVariance E): TransientHashSet<E> {
            val m = tmap.value.dissoc(key)
            if (tmap.value != m) tmap.value = m
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun contains(key: @UnsafeVariance E): Boolean =
            NOT_FOUND != tmap.value.valAt(key, NOT_FOUND as E)

        override val count: Int
            get() = tmap.value.count
    }

    companion object {
        val NOT_FOUND = Any()
    }
}
