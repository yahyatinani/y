package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) :
    PersistentSet<E> {

    override fun conj(e: @UnsafeVariance E): PersistentSet<E> = when {
        contains(e) -> this
        else -> HashSet(map.assoc(e, e))
    }

    override fun empty(): IPersistentCollection<E> = EmptyHashSet

    override fun equiv(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun seq(): ISeq<E> {
        TODO("Not yet implemented")
    }

    internal abstract class AEmptyHashSet<out E>(m: IPersistentMap<E, E>) :
        PersistentHashSet<E>(m) {
        override val count: Int = 0

        override fun contains(e: @UnsafeVariance E): Boolean = false
    }

    internal
    object EmptyHashSet : AEmptyHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal
    class HashSet<out E>(m: IPersistentMap<E, E>) : PersistentHashSet<E>(m) {

        override val count: Int = map.count

        override fun contains(e: @UnsafeVariance E): Boolean =
            map.containsKey(e)
    }

    internal class TransientHashSet<out E>(
        internal
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : ATransientSet<E>(tmap) {

        override fun persistent(): IPersistentCollection<E> =
            HashSet(tmap.value.persistent())
    }
}
