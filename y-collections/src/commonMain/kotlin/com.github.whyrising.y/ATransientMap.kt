package com.github.whyrising.y

abstract class ATransientMap<out K, out V> : ITransientMap<K, V> {

    internal abstract fun assertMutable()

    internal abstract fun doAssoc(
        key: @UnsafeVariance K, value: @UnsafeVariance V): ITransientMap<K, V>

    internal abstract fun doPersistent(): IPersistentMap<K, V>

    override fun assoc(
        key: @UnsafeVariance K, value: @UnsafeVariance V
    ): ITransientMap<K, V> = assertMutable().let {
        return doAssoc(key, value)
    }

    override fun persistent(): IPersistentMap<K, V> {
        assertMutable()

        return doPersistent()
    }

    private fun throwAllElementsMustBeEntry(entry: Any?): Unit =
        throw IllegalArgumentException(
            "All elements of the seq must be of type Map.Entry to conj: $entry")

    @Suppress("UNCHECKED_CAST")
    override fun conj(e: Any?): ITransientMap<K, V> = assertMutable().let {
        when (e) {
            null -> return this
            is Map.Entry<*, *> -> return assoc(e.key as K, e.value as V)
            is IPersistentVector<*> -> return when {
                e.count != 2 -> throw IllegalArgumentException(
                    "Vector $e count must be 2 to conj in a map.")
                else -> assoc(e.nth(0) as K, e.nth(1) as V)
            }
            else -> {
                var rtm: ITransientMap<K, V> = this

                var seq = toSeq<Any?>(e) as ISeq<Any?>
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
}
