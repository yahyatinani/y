package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) :
    IPersistentCollection<E> {

    override fun conj(e: @UnsafeVariance E): IPersistentCollection<E> {
        TODO("Not yet implemented")
    }

    override fun empty(): IPersistentCollection<E> = EmptyHashSet

    override fun equiv(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun seq(): ISeq<E> {
        TODO("Not yet implemented")
    }

    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap) {
        override val count: Int = 0
    }

    internal
    class HashSet<out E>(m: IPersistentMap<E, E>) : PersistentHashSet<E>(m) {

        override val count: Int = map.count
    }

    internal class TransientHashSet<out E>(
        internal
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : ATransientSet<E>(tmap) {

        override fun persistent(): IPersistentCollection<E> =
            HashSet(tmap.value.persistent())
    }
}
