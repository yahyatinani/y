package com.github.whyrising.y.collections.core

import com.github.whyrising.y.collections.associative.Associative
import com.github.whyrising.y.collections.associative.ILookup
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.mutable.set.TransientSet
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.set.PersistentSet

operator fun <E> ISeq<E>.component1(): E = this.first()

operator fun <E> ISeq<E>.component2(): ISeq<E> = this.rest()

fun <K, V> get(map: ILookup<K, V>?, key: K, default: V? = null): V? =
    getFrom<K, V>(map, key, default)

fun <K, V> get(map: Map<K, V>?, key: K, default: V? = null): V? =
    getFrom<K, V>(map, key, default)

fun <E> get(map: PersistentSet<E>?, key: E, default: E? = null): E? =
    getFrom<E, E>(map, key, default)

fun <E> get(map: TransientSet<E>?, key: E, default: E? = null): E? =
    getFrom<E, E>(map, key, default)

fun <K, V> getFrom(map: Any?, key: K, default: V? = null): V? = when (map) {
    null -> null
    is ILookup<*, *> -> map.valAt(key, default) as V?
    is Map<*, *> -> when {
        map.contains(key) -> map[key] as V?
        else -> default
    }
    is PersistentSet<*> -> when {
        map.contains(key) -> map[key] as V?
        else -> default
    }
    is TransientSet<*> -> when {
        map.contains(key) -> map[key] as V?
        else -> default
    }
    else -> {
        val message = "`$map` is not associative."
        throw IllegalArgumentException(message)
    }
}

fun <K, V> assoc(
    map: Associative<K, V>?,
    kv: Pair<K, V>
): Associative<K, V> = when (map) {
    null -> PersistentArrayMap(kv)
    else -> map.assoc(kv.first, kv.second)
}

@Suppress("UNCHECKED_CAST")
tailrec fun <K, V> assoc(
    map: Associative<K, V>?,
    kv: Pair<K, V>,
    vararg kvs: Pair<K, V>
): Associative<K, V> {
    val m = assoc(map, kv)
    return when {
        kvs.isNotEmpty() -> {
            val rest = kvs.copyInto(
                arrayOfNulls(kvs.size - 1),
                0,
                1,
                kvs.size
            ) as Array<out Pair<K, V>>

            assoc(m, kvs[0], *rest)
        }
        else -> m
    }
}

fun <K, V> assocIn(
    map: Associative<K, V>?,
    ks: ISeq<K>,
    v: V
): Associative<K, V> = ks.let { (k, kz) ->
    when {
        ks.count > 1 -> {
            val m = assocIn(
                getFrom<K, Associative<K, V>>(map, k),
                kz,
                v
            )
            assoc(map, k to m) as Associative<K, V>
        }
        else -> assoc(map, k to v)
    }
}
