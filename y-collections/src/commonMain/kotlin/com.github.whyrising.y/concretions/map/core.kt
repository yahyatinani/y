package com.github.whyrising.y.concretions.map

import com.github.whyrising.y.associative.ILookup
import com.github.whyrising.y.mutable.set.TransientSet
import com.github.whyrising.y.set.PersistentSet

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
