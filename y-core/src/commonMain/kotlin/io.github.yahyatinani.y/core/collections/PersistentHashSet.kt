package io.github.yahyatinani.y.core.collections

import kotlinx.serialization.Serializable

@Serializable(with = PersistentHashSetSerializer::class)
sealed class PersistentHashSet<out E>(map: IPersistentMap<E, E>) :
  APersistentSet<E>(map), IMutableCollection<E> {

  override fun conj(e: @UnsafeVariance E): PersistentHashSet<E> = when {
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
    TransientHashSet((map as PersistentHashMap<E, E>).asTransient())

  internal abstract class AEmptyHashSet<out E>(m: IPersistentMap<E, E>) :
    PersistentHashSet<E>(m) {

    override val count: Int = 0

    override fun contains(element: @UnsafeVariance E): Boolean = false

    override fun toString(): String = "#{}"

    override fun hashCode(): Int = 0
  }

  internal object EmptyHashSet : AEmptyHashSet<Nothing>(
    PersistentHashMap.EmptyHashMap,
  )

  internal class HashSet<out E>(m: IPersistentMap<E, E>) : PersistentHashSet<E>(
    m,
  )

  internal class TransientHashSet<out E>(tmap: TransientMap<E, E>) :
    ATransientSet<E>(tmap) {

    override fun persistent(): IPersistentCollection<E> =
      HashSet(transientMap.persistent())
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

    internal fun <E> create(set: Set<E>): PersistentHashSet<E> {
      var transient = EmptyHashSet.asTransient() as TransientSet<E>

      for (e in set) transient = transient.conj(e) as TransientSet<E>

      return transient.persistent() as PersistentHashSet
    }

    internal fun <E> createWithCheck(vararg e: E): PersistentHashSet<E> {
      var transient = EmptyHashSet.asTransient() as TransientSet<E>

      for (i in e.indices) {
        transient = transient.conj(e[i]) as TransientSet<E>

        if (transient.count != i + 1) {
          throw IllegalArgumentException("Duplicate key: ${e[i]}")
        }
      }

      return transient.persistent() as PersistentHashSet
    }
  }
}
