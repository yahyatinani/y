package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) :
    PersistentSet<E>, Set<E>, IMutableCollection<E> {

    override fun conj(e: @UnsafeVariance E): PersistentSet<E> = when {
        contains(e) -> this
        else -> HashSet(map.assoc(e, e))
    }

    override fun empty(): IPersistentCollection<E> = EmptyHashSet

    @Suppress("UNCHECKED_CAST")
    override fun equiv(other: Any?): Boolean {
        when {
            this === other -> return true
            other !is Set<*> -> return false
            count != other.size -> return false
            else -> for (e in other) if (!contains(e as E)) return false
        }

        return true
    }

    override fun seq(): ISeq<E> = map.keyz()

    // Set Implementation
    override val size: Int
        get() = count

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        for (e in elements)
            if (!contains(e)) return false

        return true
    }

    override fun isEmpty(): Boolean = count == 0

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<E> = when (map) {
        is MapIterable<*, *> -> map.keyIterator() as Iterator<E>
        else -> object : Iterator<E> {
            val iter = map.iterator()

            override fun hasNext(): Boolean = iter.hasNext()

            override fun next(): E = (iter.next() as MapEntry<E, E>).key
        }
    }

    override fun asTransient(): ITransientCollection<E> =
        TransientHashSet(atomic((map as LeanMap<E, E>).asTransient()))

    internal abstract class AEmptyHashSet<out E>(m: IPersistentMap<E, E>) :
        PersistentHashSet<E>(m) {
        override val count: Int = 0

        override fun contains(element: @UnsafeVariance E): Boolean = false
    }

    internal
    object EmptyHashSet : AEmptyHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal
    class HashSet<out E>(m: IPersistentMap<E, E>) : PersistentHashSet<E>(m) {

        override val count: Int = map.count

        override fun contains(element: @UnsafeVariance E): Boolean =
            map.containsKey(element)
    }

    internal class TransientHashSet<out E>(
        internal
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : ATransientSet<E>(tmap) {

        override fun persistent(): IPersistentCollection<E> =
            HashSet(tmap.value.persistent())
    }

    companion object {
        internal fun <E> create(vararg e: E): PersistentHashSet<E> {
            var transient = EmptyHashSet.asTransient() as TransientSet<E>

            for (i in e.indices)
                transient = transient.conj(e[i]) as TransientSet<E>

            return transient.persistent() as PersistentHashSet
        }

        internal fun <E> createWithCheck(vararg e: E): PersistentHashSet<E> {
            var transient = EmptyHashSet.asTransient() as TransientSet<E>

            for (i in e.indices) {
                transient = transient.conj(e[i]) as TransientSet<E>

                if (transient.count != i + 1)
                    throw IllegalArgumentException("Duplicate key: ${e[i]}")
            }

            return transient.persistent() as PersistentHashSet
        }
    }
}
