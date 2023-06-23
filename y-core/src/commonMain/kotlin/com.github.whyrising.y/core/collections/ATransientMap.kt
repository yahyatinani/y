package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.seq

abstract class ATransientMap<out K, out V> :
  TransientMap<K, V>,
  TransientAssociative2<K, V> {

  internal abstract fun ensureEditable()

  internal abstract fun doAssoc(
    key: @UnsafeVariance K,
    value: @UnsafeVariance V,
  ): TransientMap<K, V>

  internal abstract fun doDissoc(key: @UnsafeVariance K): TransientMap<K, V>

  internal abstract fun doPersistent(): IPersistentMap<K, V>

  internal abstract fun doValAt(
    key: @UnsafeVariance K,
    default: @UnsafeVariance V?,
  ): V?

  internal abstract val doCount: Int

  override fun assoc(
    key: @UnsafeVariance K,
    value: @UnsafeVariance V,
  ): TransientMap<K, V> = ensureEditable().let {
    return doAssoc(key, value)
  }

  override fun dissoc(key: @UnsafeVariance K): TransientMap<K, V> {
    ensureEditable()

    return doDissoc(key)
  }

  override fun persistent(): IPersistentMap<K, V> {
    ensureEditable()

    return doPersistent()
  }

  private fun throwAllElementsMustBeEntry(entry: Any?): Unit =
    throw IllegalArgumentException(
      "All elements of the seq must be of type Map.Entry to conj: $entry",
    )

  @Suppress("UNCHECKED_CAST")
  override fun conj(e: Any?): TransientMap<K, V> = ensureEditable().let {
    when (e) {
      null -> return this
      is Map.Entry<*, *> -> return assoc(e.key as K, e.value as V)
      is IPersistentVector<*> -> return when {
        e.count != 2 -> throw IllegalArgumentException(
          "Vector $e count must be 2 to conj in a map.",
        )
        else -> assoc(e.nth(0) as K, e.nth(1) as V)
      }
      else -> {
        var rtm: TransientMap<K, V> = this

        var seq = seq(e) as ISeq<Any?>
        for (i in 0 until seq.count)
          when (val entry = seq.first()) {
            is Map.Entry<*, *> -> {
              rtm = rtm.assoc(entry.key as K, entry.value as V)
              seq = seq.rest()
            }
            else -> throwAllElementsMustBeEntry(entry)
          }

        return rtm
      }
    }
  }

  override val count: Int
    get() {
      ensureEditable()
      return doCount
    }

  override fun valAt(key: @UnsafeVariance K): V? = valAt(key, null)

  override fun valAt(key: @UnsafeVariance K, default: @UnsafeVariance V?): V? {
    ensureEditable()
    return doValAt(key, default)
  }

  private object TOKEN : Any()

  @Suppress("UNCHECKED_CAST")
  override fun containsKey(key: @UnsafeVariance K): Boolean {
    val r = valAt(key, TOKEN as V)

    return r != TOKEN
  }

  @Suppress("UNCHECKED_CAST")
  override fun entryAt(key: @UnsafeVariance K): IMapEntry<K, V>? =
    valAt(key, TOKEN as V).let { valAtKey: V? ->
      if (valAtKey == TOKEN) return null

      return MapEntry(key, valAtKey) as IMapEntry<K, V>
    }

  operator fun invoke(
    key: @UnsafeVariance K,
    default: @UnsafeVariance V?,
  ): V? = valAt(key, default)

  operator fun invoke(key: @UnsafeVariance K): V? = valAt(key)
}
