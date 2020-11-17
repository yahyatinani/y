package com.github.whyrising.y

import com.github.whyrising.y.PersistentHashSet.Companion.create
import com.github.whyrising.y.PersistentHashSet.Companion.createWithCheck
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

sealed class PersistentHashSet<out E>(map: IPersistentMap<E, E>) :
    APersistentSet<E>(map), IMutableCollection<E> {

    override fun conj(e: @UnsafeVariance E): PersistentSet<E> = when {
        contains(e) -> this
        else -> HashSet(map.assoc(e, e))
    }

    override fun empty(): IPersistentCollection<E> = EmptyHashSet

    override fun disjoin(e: @UnsafeVariance E): PersistentSet<E> = when {
        contains(e) -> {
            val m = map.dissoc(e)
            when {
                m.count > 0 -> HashSet(m)
                else -> EmptyHashSet
            }
        }
        else -> this
    }

    override fun asTransient(): ITransientCollection<E> =
        TransientHashSet(atomic((map as LeanMap<E, E>).asTransient()))

    internal abstract class AEmptyHashSet<out E>(m: IPersistentMap<E, E>) :
        PersistentHashSet<E>(m) {

        override val count: Int = 0

        override fun contains(element: @UnsafeVariance E): Boolean = false

        override fun toString(): String = "#{}"

        override fun hashCode(): Int = 0

        override fun equals(other: Any?): Boolean = this === other
    }

    internal
    object EmptyHashSet : AEmptyHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal
    class HashSet<out E>(m: IPersistentMap<E, E>) : PersistentHashSet<E>(m)

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

        internal fun <E> create(seq: ISeq<E>): PersistentHashSet<E> {
            var transient = EmptyHashSet.asTransient() as TransientSet<E>
            var elements = seq

            while (elements.count != 0) {
                transient = transient.conj(elements.first()) as TransientSet<E>
                elements = elements.rest()
            }

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

fun <E> hashSet(): PersistentHashSet<E> = PersistentHashSet.EmptyHashSet

fun <E> hashSet(vararg e: E) = create(*e)

fun <E> hashSet(seq: ISeq<E>): PersistentHashSet<E> = create(seq)

fun <E> hs(): PersistentHashSet<E> = PersistentHashSet.EmptyHashSet

fun <E> hs(vararg e: E): PersistentHashSet<E> = createWithCheck(*e)
