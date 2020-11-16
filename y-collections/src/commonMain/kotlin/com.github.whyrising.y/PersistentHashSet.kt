package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) {
    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal class TransientHashSet<out E>(
        internal
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : TransientSet<E> {

        override val count: Int
            get() = tmap.value.count

        override fun disjoin(key: @UnsafeVariance E): TransientSet<E> {
            val m = tmap.value.dissoc(key)
            if (tmap.value != m) tmap.value = m
            return this
        }

        @Suppress("UNCHECKED_CAST")
        override fun contains(key: @UnsafeVariance E): Boolean =
            NOT_FOUND != tmap.value.valAt(key, NOT_FOUND as E)

        override
        operator fun get(key: @UnsafeVariance E): E? = tmap.value.valAt(key)

        override fun conj(e: @UnsafeVariance E): TransientSet<E> {
            val m = tmap.value.assoc(e, e)

            if (m != tmap.value) tmap.value = m

            return this
        }

        override fun persistent(): IPersistentCollection<E> {
            TODO("Not yet implemented")
        }
    }

    companion object {
        val NOT_FOUND = Any()
    }
}
