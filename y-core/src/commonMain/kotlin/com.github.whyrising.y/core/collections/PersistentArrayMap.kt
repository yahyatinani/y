package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.util.equiv
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.atomicfu.update
import kotlinx.serialization.Serializable
import kotlin.collections.Map.Entry
import kotlin.math.max

const val HASHTABLE_THRESHOLD = 16

@Serializable(PersistentArrayMapSerializer::class)
class PersistentArrayMap<out K, out V> internal constructor(
  internal val array: Array<Any?>
) : APersistentMap<K, V>(), MapIterable<K, V>, IMutableCollection<Any?> {

  @Suppress("UNCHECKED_CAST")
  private fun <K, V> createArrayMap(newPairs: Array<Any?>) =
    PersistentArrayMap<K, V>(newPairs)

  private fun indexOf(key: @UnsafeVariance K): Int {
    for (i in array.indices step 2)
      if (equiv(key, array[i])) return i

    return -1
  }

  private fun keyExists(index: Int): Boolean = index >= 0

  override fun assoc(
    key: @UnsafeVariance K,
    value: @UnsafeVariance V
  ): IPersistentMap<K, V> {
    val index: Int = indexOf(key)
    val newPairs: Array<Any?>

    when {
      keyExists(index) -> {
        if (array[index + 1] == value) return this

        newPairs = array.copyOf()
        newPairs[index + 1] = value
      }
      else -> {
        if (array.size >= HASHTABLE_THRESHOLD) {
          return PersistentHashMap.create<K, V>(array)
            .assoc(key, value)
        }

        newPairs = arrayOfNulls(array.size + 2)

        if (array.isNotEmpty()) {
          array.copyInto(newPairs, 0, 0, array.size)
        }

        newPairs[newPairs.size - 2] = key
        newPairs[newPairs.size - 1] = value
      }
    }

    return createArrayMap(newPairs)
  }

  override fun assocNew(
    key: @UnsafeVariance K,
    value: @UnsafeVariance V
  ): IPersistentMap<K, V> {
    val index: Int = indexOf(key)

    if (keyExists(index)) {
      throw RuntimeException("The key $key is already present.")
    }

    if (count >= HASHTABLE_THRESHOLD) {
      return PersistentHashMap.createWithCheck<K, V>(array)
        .assocNew(key, value)
    }

    val newPairs: Array<Any?> = arrayOfNulls(array.size + 2)

    if (array.isNotEmpty()) {
      array.copyInto(newPairs, 2, 0, array.size)
    }

    newPairs[0] = key
    newPairs[1] = value

    return createArrayMap(newPairs)
  }

  override fun dissoc(key: @UnsafeVariance K): IPersistentMap<K, V> =
    indexOf(key).let { index ->
      when {
        keyExists(index) -> {
          val newSize = array.size - 2

          if (newSize == 0) {
            return EmptyArrayMap
          }

          val newPairs: Array<Any?> = arrayOfNulls(newSize)
          array.copyInto(newPairs, 0, 0, index)
          array.copyInto(newPairs, index, index + 2, array.size)

          return createArrayMap(newPairs)
        }
        else -> return this
      }
    }

  override fun containsKey(key: @UnsafeVariance K): Boolean =
    keyExists(indexOf(key))

  @Suppress("UNCHECKED_CAST")
  override fun entryAt(
    key: @UnsafeVariance K
  ): IMapEntry<K, V>? = indexOf(key).let { index ->
    when {
      keyExists(index) -> MapEntry(
        array[index] as K,
        array[index + 1] as V
      )
      else -> null
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun valAt(
    key: @UnsafeVariance K,
    default: @UnsafeVariance V?
  ): V? = indexOf(key).let { index ->
    when {
      keyExists(index) -> array[index + 1] as V
      else -> default
    }
  }

  override fun valAt(key: @UnsafeVariance K): V? = valAt(key, null)

  override fun seq(): ISeq<MapEntry<K, V>> = when (count) {
    0 -> PersistentList.Empty
    else -> Seq(array, 0)
  }

  override val count: Int = array.size / 2

  override fun empty(): IPersistentCollection<Any?> = EmptyArrayMap

  override fun iterator(): Iterator<Entry<K, V>> = Iter(array, makeMapEntry)

  override fun keyIterator(): Iterator<K> = Iter(array, makeKey)

  override fun valIterator(): Iterator<V> = Iter(array, makeValue)

  override fun asTransient(): TransientMap<K, V> = TransientArrayMap(array)

  internal class Iter<K, V, R>(
    private val array: Array<Any?>,
    val f: (k: K, v: V) -> R
  ) : Iterator<R> {

    var index = 0

    override fun hasNext(): Boolean = index <= array.size - 2

    @Suppress("UNCHECKED_CAST")
    override fun next(): R = when {
      index > array.size - 2 -> throw NoSuchElementException()
      else -> {
        val cached = index
        index += 2
        f(array[cached] as K, array[cached + 1] as V)
      }
    }
  }

  internal class Seq<out K, out V>(
    private val array: Array<Any?>,
    val index: Int
  ) : ASeq<MapEntry<K, V>>() {

    override val count: Int = (array.size - index) / 2

    @Suppress("UNCHECKED_CAST")
    override fun first(): MapEntry<K, V> = MapEntry(
      array[index] as K,
      array[index + 1] as V
    )

    override fun next(): ISeq<MapEntry<K, V>>? = when {
      index + 2 < array.size -> Seq(array, index + 2)
      else -> null
    }
  }

  internal class TransientArrayMap<out K, out V> private constructor(
    internal val array: Array<Any?>,
    edit: Any?,
    length: Int
  ) : ATransientMap<K, V>() {
    private val _edit: AtomicRef<Any?> = atomic(edit)
    private val _length = atomic(length)

    constructor(array: Array<Any?>) : this(
      array.copyOf(max(HASHTABLE_THRESHOLD, array.size)),
      Any(),
      length = array.size
    )

    val edit by _edit
    val length by _length

    override val doCount: Int
      get() = length / 2

    override fun ensureEditable() {
      if (_edit.value == null) {
        throw IllegalStateException(
          "Transient used after persistent() call."
        )
      }
    }

    private fun indexOf(key: @UnsafeVariance K): Int {
      for (i in 0 until length step 2)
        if (equiv(key, array[i])) return i

      return -1
    }

    private val lock = reentrantLock()

    @Suppress("UNCHECKED_CAST")
    override fun doAssoc(
      key: @UnsafeVariance K,
      value: @UnsafeVariance V
    ): TransientMap<K, V> {
      lock.withLock {
        ensureEditable()

        val index = indexOf(key)
        when {
          index >= 0 -> {
            if (array[index + 1] != value) {
              array[index + 1] = value
            }
          }
          length >= array.size -> {
            return PersistentHashMap.create<K, V>(array)
              .asTransient()
              .assoc(key, value)
          }
          else -> {
            array[_length.getAndIncrement()] = key
            array[_length.getAndIncrement()] = value
          }
        }

        return this
      }
    }

    override fun doDissoc(key: @UnsafeVariance K): TransientMap<K, V> {
      lock.withLock {
        val index: Int = indexOf(key)

        if (index >= 0) {
          _length.update { currentLength ->
            if (currentLength >= 2) {
              array[index] = array[currentLength - 2]
              array[index + 1] = array[currentLength - 1]
            }

            currentLength - 2
          }
        }
        return this
      }
    }

    @Suppress("UNCHECKED_CAST")
    override fun doPersistent(): IPersistentMap<K, V> {
      ensureEditable()

      _edit.value = null
      val ar = arrayOfNulls<Any?>(length)
      array.copyInto(ar, 0, 0, length)

      return PersistentArrayMap(ar)
    }

    @Suppress("UNCHECKED_CAST")
    override fun doValAt(
      key: @UnsafeVariance K,
      default: @UnsafeVariance V?
    ): V? = indexOf(key).let { index ->
      when {
        index >= 0 -> array[index + 1] as V
        else -> default
      }
    }
  }

  companion object {
    internal val EmptyArrayMap =
      PersistentArrayMap<Nothing, Nothing>(emptyArray())

    private fun <K> areKeysEqual(key1: K, key2: K): Boolean = when (key1) {
      key2 -> true
      else -> equiv(key1, key2)
    }

    internal fun <K, V> createWithCheck(
      vararg kvs: Any?
    ): PersistentArrayMap<K, V> {
      for (i in kvs.indices step 2)
        for (j in i + 2 until kvs.size step 2)
          if (areKeysEqual(kvs[i], kvs[j])) {
            throw IllegalArgumentException("Duplicate key: ${kvs[i]}")
          }

      return PersistentArrayMap(kvs as Array<Any?>)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <K, V> createWithCheck(
      vararg pairs: Pair<K, V>
    ): PersistentArrayMap<K, V> {
      val entries = arrayOfNulls<Any?>(pairs.size * 2)
      var i = 0
      for ((f, s) in pairs) {
        entries[i] = f
        entries[i + 1] = s
        i += 2
      }

      return createWithCheck(*entries)
    }

    internal fun <K, V> create(map: Map<K, V>): IPersistentMap<K, V> {
      var ret: TransientMap<K, V> = EmptyArrayMap.asTransient()

      for (entry in map.entries)
        ret = ret.assoc(entry.key, entry.value)

      return ret.persistent()
    }
  }
}
