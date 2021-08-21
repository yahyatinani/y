package com.github.whyrising.y.collections.core

import com.github.whyrising.y.collections.ArraySeq
import com.github.whyrising.y.collections.associative.Associative
import com.github.whyrising.y.collections.associative.ILookup
import com.github.whyrising.y.collections.concretions.list.Cons
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.map.HASHTABLE_THRESHOLD
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.mutable.set.TransientSet
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.Seqable
import com.github.whyrising.y.collections.set.PersistentSet
import com.github.whyrising.y.collections.util.lazyChunkedSeq
import com.github.whyrising.y.collections.vector.IPersistentVector

@Suppress("UNCHECKED_CAST")
fun <E> seq(x: Any?): ISeq<E>? = when (x) {
    null -> null
    is ISeq<*> -> x as ISeq<E>
    is Seqable<*> -> x.seq() as ISeq<E>
    is Iterable<*> -> lazyChunkedSeq(x.iterator() as Iterator<E>)
    is ShortArray -> ArraySeq(x) as ISeq<E>
    is IntArray -> ArraySeq(x) as ISeq<E>
    is FloatArray -> ArraySeq(x) as ISeq<E>
    is DoubleArray -> ArraySeq(x) as ISeq<E>
    is LongArray -> ArraySeq(x) as ISeq<E>
    is ByteArray -> ArraySeq(x) as ISeq<E>
    is CharArray -> ArraySeq(x) as ISeq<E>
    is BooleanArray -> ArraySeq(x) as ISeq<E>
    is Array<*> -> ArraySeq(x) as ISeq<E>
    is CharSequence -> TODO("needs StringSeq class")
    is Map<*, *> -> seq(x.entries)
    else -> throw IllegalArgumentException(
        "Don't know how to create ISeq from: ${x::class.simpleName}"
    )
}

operator fun <E> ISeq<E>.component1(): E = this.first()

operator fun <E> ISeq<E>.component2(): ISeq<E> = this.rest()

fun <K, V> Map<K, V>.toPmap(): IPersistentMap<K, V> =
    PersistentArrayMap.create(this)

fun <K, V> m(vararg kvs: Pair<K, V>): IPersistentMap<K, V> = when {
    kvs.isEmpty() -> PersistentArrayMap.EmptyArrayMap
    kvs.size <= HASHTABLE_THRESHOLD -> PersistentArrayMap.createWithCheck(*kvs)
    else -> PersistentHashMap.createWithCheck(*kvs)
}

fun <K, V> hashMap(vararg kvs: Pair<K, V>): PersistentHashMap<K, V> = when {
    kvs.isEmpty() -> PersistentHashMap.EmptyHashMap
    else -> PersistentHashMap.create(*kvs)
}

fun <E> cons(x: E, coll: Any?): ISeq<E> = when (coll) {
    null -> PersistentList()
    is ISeq<*> -> Cons(x, coll) as ISeq<E>
    else -> Cons(x, seq<E>(coll) as ISeq<E>)
}

fun <E> v(): IPersistentVector<E> = PersistentVector()

fun <E> v(a: E): IPersistentVector<E> = PersistentVector(a)

fun <E> v(a: E, b: E): IPersistentVector<E> = PersistentVector(a, b)

fun <E> v(a: E, b: E, c: E): IPersistentVector<E> = PersistentVector(a, b, c)

fun <E> v(a: E, b: E, c: E, d: E): IPersistentVector<E> =
    PersistentVector(a, b, c, d)

fun <E> v(a: E, b: E, c: E, d: E, e: E): IPersistentVector<E> =
    PersistentVector(a, b, c, d, e)

fun <E> v(a: E, b: E, c: E, d: E, e: E, f: E): IPersistentVector<E> =
    PersistentVector(a, b, c, d, e, f)

fun <E> v(
    a: E,
    b: E,
    c: E,
    d: E,
    e: E,
    f: E,
    vararg args: E
): IPersistentVector<E> = PersistentVector(
    cons(a, cons(b, cons(c, cons(d, cons(e, cons(f, args))))))
)

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
    null -> PersistentArrayMap(arrayOf(kv))
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

@Suppress("UNCHECKED_CAST")
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
