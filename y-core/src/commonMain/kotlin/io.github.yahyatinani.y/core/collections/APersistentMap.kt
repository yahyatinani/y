package io.github.yahyatinani.y.core.collections

import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.fold
import io.github.yahyatinani.y.core.seq
import io.github.yahyatinani.y.core.util.Murmur3
import io.github.yahyatinani.y.core.util.equiv
import kotlin.collections.Map.Entry

abstract class APersistentMap<out K, out V> :
  IPersistentMap<K, V>,
  Map<@UnsafeVariance K, V>,
  Iterable<Entry<K, V>>,
  MapEquivalence,
  IHashEq {

  @Suppress("UNCHECKED_CAST")
  internal val hashCode: Int by lazy {
    var hash = 0
    var seq =
      seq() as ISeq<MapEntry<K, V>>
    while (seq != Empty) {
      val entry = seq.first()
      hash += entry.key.hashCode() xor entry.value.hashCode()
      seq = seq.rest()
    }
    hash
  }

  internal val hasheq: Int by lazy { Murmur3.hashUnordered(this) }

  @Suppress("UNCHECKED_CAST")
  override fun toString(): String {
    var seq =
      seq() as ISeq<MapEntry<K, V>>
    var s = "{"

    while (seq != Empty) {
      val entry = seq.first()
      s += "${entry.key} ${entry.value}"
      seq = seq.rest()

      if (seq != Empty) s += ", "
    }

    s += '}'

    return s
  }

  override fun hashCode(): Int = hashCode

  override fun hasheq(): Int = hasheq

  @Suppress("UNCHECKED_CAST")
  override fun equals(other: Any?): Boolean {
    when {
      this === other -> return true
      other !is Map<*, *> -> return false
      count != other.size -> return false
      else -> {
        var seq = seq()
        val map = other as Map<K, V>

        for (i in 0 until count) {
          val entry = seq.first() as Entry<K, V>
          val key = entry.key
          val keyFound = map.containsKey(key)

          if (!keyFound || entry.value != map.getValue(key)) {
            return false
          }

          seq = seq.rest()
        }

        return true
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun conj(e: Any?): IPersistentCollection<Any?> = when (e) {
    null -> this
    is Entry<*, *> -> assoc(e.key as K, e.value as V)
    is IPersistentVector<*> -> when {
      e.count != 2 -> throw IllegalArgumentException(
        "Vector $e count should be 2 to conj in a map",
      )

      else -> assoc(e.nth(0) as K, e.nth(1) as V)
    }

    else -> when (val seq: ISeq<Any?>? = seq(e)) {
      null -> this
      else -> seq.fold(this) { acc, entry ->
        if (entry !is Entry<*, *>) {
          throw IllegalArgumentException(
            "All elements of the seq must be of type Map.Entry " +
              "to conj: $entry",
          )
        }

        acc.assoc(
          entry.key as K,
          entry.value as V,
        ) as APersistentMap<K, V>
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun equiv(other: Any?): Boolean {
    when {
      this === other -> return true
      other !is Map<*, *> -> return false
      other is IPersistentMap<*, *> && other !is MapEquivalence -> return false

      count != other.size -> return false
      else -> {
        var seq = seq()
        val map = other as Map<K, V>

        for (i in 0 until count) {
          val entry = seq.first() as Entry<K, V>
          val key = entry.key
          val keyFound = map.containsKey(key)

          if (!keyFound || !equiv(entry.value, map.getValue(key))) {
            return false
          }

          seq = seq.rest()
        }

        return true
      }
    }
  }

  operator fun invoke(key: @UnsafeVariance K, default: @UnsafeVariance V?):
    V? = valAt(key, default)

  operator fun invoke(key: @UnsafeVariance K): V? = valAt(key)

  override fun keyz(): ISeq<K> = KeySeq(this)

  override fun vals(): ISeq<V> = ValSeq(this)

  // Map implementation
  override val size: Int
    get() = count

  override fun isEmpty(): Boolean = count == 0

  override fun containsValue(value: @UnsafeVariance V): Boolean =
    values.contains(value)

  override fun get(key: @UnsafeVariance K): V? = valAt(key)

  override val keys: Set<K>
    get() = object : AbstractSet<K>(), Set<K> {

      override val size: Int
        get() = count

      override fun contains(element: @UnsafeVariance K): Boolean =
        containsKey(element)

      override fun iterator(): Iterator<K> {
        val mapIter = this@APersistentMap.iterator()

        return object : Iterator<K> {

          override fun hasNext(): Boolean = mapIter.hasNext()

          override fun next(): K = mapIter.next().key
        }
      }
    }

  override val values: Collection<V>
    get() = object : AbstractCollection<V>() {

      override val size: Int
        get() = count

      override fun iterator(): Iterator<V> {
        val mapIter = this@APersistentMap.iterator()

        return object : Iterator<V> {

          override fun hasNext(): Boolean = mapIter.hasNext()

          override fun next(): V = mapIter.next().value
        }
      }
    }

  override val entries: Set<Entry<K, V>>
    get() = object : AbstractSet<Entry<K, V>>() {
      override val size: Int
        get() = count

      @Suppress("USELESS_IS_CHECK")
      override fun contains(
        element: Entry<@UnsafeVariance K, @UnsafeVariance V>,
      ): Boolean = when (element) {
        !is Entry<K, V> -> false
        else -> {
          val e = entryAt(element.key)
          e != null && e.value == element.value
        }
      }

      override fun hashCode(): Int = this@APersistentMap.hashCode()

      override fun iterator(): Iterator<Entry<K, V>> =
        this@APersistentMap.iterator()
    }

  protected val makeMapEntry: (
    @UnsafeVariance K,
    @UnsafeVariance V,
  ) -> MapEntry<K, V> =
    { k, v ->
      MapEntry(k, v)
    }

  protected val makeKey: (@UnsafeVariance K, @UnsafeVariance V) -> K =
    { k, _ -> k }

  protected val makeValue: (@UnsafeVariance K, @UnsafeVariance V) -> V =
    { _, v -> v }

  internal class KeySeq<out K, out V> private constructor(
    internal val _seq: ISeq<K>,
    val map: Iterable<Entry<K, V>>?,
  ) : ASeq<K>() {

    @Suppress("UNCHECKED_CAST")
    override fun first(): K = (_seq.first() as Entry<K, V>).key

    override fun next(): ISeq<K>? =
      when {
        count > 1 -> KeySeq<K, V>(
          _seq.rest(),
          null,
        )

        else -> null
      }

    override val count: Int
      get() = _seq.count

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<K> = when (map) {
      null -> super.iterator()
      is MapIterable<*, *> -> (map as MapIterable<K, V>).keyIterator()
      else -> map.iterator().let { mapIter ->
        object : Iterator<K> {

          override fun hasNext(): Boolean = mapIter.hasNext()

          override fun next(): K = mapIter.next().key
        }
      }
    }

    companion object {
      @Suppress("UNCHECKED_CAST")
      operator fun <K, V> invoke(map: IPersistentMap<K, V>): KeySeq<K, V> =
        KeySeq(map.seq() as ISeq<K>, map)
    }
  }

  internal class ValSeq<out K, out V> private constructor(
    internal val _seq: ISeq<V>,
    val map: Iterable<Entry<K, V>>?,
  ) : ASeq<V>() {

    @Suppress("UNCHECKED_CAST")
    override fun first(): V = (_seq.first() as Entry<K, V>).value

    override fun next(): ISeq<V>? =
      when {
        count > 1 -> ValSeq<K, V>(
          _seq.rest(),
          null,
        )

        else -> null
      }

    override val count: Int = _seq.count

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<V> = when (map) {
      null -> super.iterator()
      is MapIterable<*, *> -> (map as MapIterable<K, V>).valIterator()
      else -> map.iterator().let { mapIter ->
        object : Iterator<V> {

          override fun hasNext(): Boolean = mapIter.hasNext()

          override fun next(): V = mapIter.next().value
        }
      }
    }

    companion object {
      @Suppress("UNCHECKED_CAST")
      operator fun <K, V> invoke(map: IPersistentMap<K, V>): ValSeq<K, V> =
        ValSeq(map.seq() as ISeq<V>, map)
    }
  }
}
