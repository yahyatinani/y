package com.github.whyrising.y

sealed class PersistentArrayMap<out K, out V> {

    object EmptyArrayMap : PersistentArrayMap<Nothing, Nothing>() {

        override fun toString(): String = "{}"
    }

    internal class ArrayMap<out K, out V>(
        internal val pairs: Array<out Pair<K, V>>
    ) : PersistentArrayMap<K, V>()

    companion object {
        operator fun <K, V> invoke(): PersistentArrayMap<K, V> = EmptyArrayMap

        operator
        fun <K, V> invoke(vararg pairs: Pair<K, V>): PersistentArrayMap<K, V> {
            for (i in pairs.indices)
                for (j in i + 1 until pairs.size)
                    if (pairs[i].first == pairs[j].first)
                        throw IllegalArgumentException("Duplicate key: $i")

            return ArrayMap(pairs)
        }
    }
}
