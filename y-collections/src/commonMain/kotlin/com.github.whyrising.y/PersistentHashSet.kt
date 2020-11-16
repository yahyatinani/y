package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) :
    IPersistentCollection<E> {

    override val count: Int
        get() = TODO("Not yet implemented")

    override fun conj(e: @UnsafeVariance E): IPersistentCollection<E> {
        TODO("Not yet implemented")
    }

    override fun empty(): IPersistentCollection<E> {
        TODO("Not yet implemented")
    }

    override fun equiv(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun seq(): ISeq<E> {
        TODO("Not yet implemented")
    }

    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal class HashSet<out E>(_map: IPersistentMap<E, E>) :
        PersistentHashSet<E>(_map)

    internal class TransientHashSet<out E>(
        internal
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : ATransientSet<E>(tmap) {

        override fun persistent(): IPersistentCollection<E> =
            HashSet(tmap.value.persistent())
    }
}
