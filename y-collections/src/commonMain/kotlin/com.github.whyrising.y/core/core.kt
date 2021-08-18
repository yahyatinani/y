package com.github.whyrising.y.core

import com.github.whyrising.y.associative.Associative
import com.github.whyrising.y.concretions.map.PersistentArrayMap

//fun <K, V> IPersistentMap<K, V>.assocIn(
//    ks: ISeq<K>,
//    v: V
//): IPersistentMap<K, V> = when (ks.count) {
//    0 -> throw IllegalArgumentException("`ks` must have at least 1 key")
//    1 -> this.assoc(ks.first(), v)
//    else -> {
//        val key = ks.first()
//        val m = this.valAt(key) as IPersistentMap<Any, Any>? ?: m()
//        this.assoc(key, m.assocIn(ks.rest(), v) as V)
//    }
//}
//

//@JvmName("assocIn1")
//fun <K, V> assocIn(
//    map: IPersistentMap<K, V>,
//    ks: ISeq<K>,
//    v: V
//): IPersistentMap<K, V> = map.assocIn(ks, v)

fun <K, V> assoc(
    map: Any?,
    kv: Pair<K, V>
): Associative<K, V> = when (map) {
    null -> PersistentArrayMap(kv)
    is Associative<*, *> -> map.assoc(kv.first, kv.second) as Associative<K, V>
    else -> throw IllegalArgumentException("$map is not Associative")
}

tailrec fun <K, V> assoc(
    map: Any?,
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
