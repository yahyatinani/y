package com.github.whyrising.y.collections.core

import com.github.whyrising.y.collections.ArraySeq
import com.github.whyrising.y.collections.Chunk
import com.github.whyrising.y.collections.PersistentQueue
import com.github.whyrising.y.collections.StringSeq
import com.github.whyrising.y.collections.associative.Associative
import com.github.whyrising.y.collections.associative.ILookup
import com.github.whyrising.y.collections.concretions.list.ASeq
import com.github.whyrising.y.collections.concretions.list.ChunkedSeq
import com.github.whyrising.y.collections.concretions.list.Cons
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.concretions.map.HASHTABLE_THRESHOLD
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import com.github.whyrising.y.collections.concretions.set.PersistentHashSet
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.mutable.set.TransientSet
import com.github.whyrising.y.collections.seq.IChunkedSeq
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.LazySeq
import com.github.whyrising.y.collections.seq.Seqable
import com.github.whyrising.y.collections.set.PersistentSet
import com.github.whyrising.y.collections.util.lazyChunkedSeq
import com.github.whyrising.y.collections.vector.IPersistentVector

@Suppress("UNCHECKED_CAST")
fun <E> seq(coll: Any?): ISeq<E>? = when (coll) {
    null, Empty -> null
    is ASeq<*> -> coll as ASeq<E>
    is LazySeq<*> -> when (val seq = coll.seq()) {
        is Empty -> null
        else -> seq as ISeq<E>
    }
    is Seqable<*> -> coll.seq() as ISeq<E>
    is Iterable<*> -> lazyChunkedSeq(coll.iterator() as Iterator<E>)
    is Sequence<*> -> lazyChunkedSeq(coll.iterator() as Iterator<E>)
    is ShortArray -> ArraySeq(coll) as ISeq<E>
    is IntArray -> ArraySeq(coll) as ISeq<E>
    is FloatArray -> ArraySeq(coll) as ISeq<E>
    is DoubleArray -> ArraySeq(coll) as ISeq<E>
    is LongArray -> ArraySeq(coll) as ISeq<E>
    is ByteArray -> ArraySeq(coll) as ISeq<E>
    is CharArray -> ArraySeq(coll) as ISeq<E>
    is BooleanArray -> ArraySeq(coll) as ISeq<E>
    is Array<*> -> ArraySeq(coll) as ISeq<E>
    is CharSequence -> StringSeq(coll) as ISeq<E>
    is Map<*, *> -> seq(coll.entries)
    else -> throw IllegalArgumentException(
        "Don't know how to create ISeq from: ${coll::class.simpleName}"
    )
}

fun <E> l(): PersistentList<E> = Empty

fun <E> l(vararg elements: E): PersistentList<E> = PersistentList(*elements)

fun <E> List<E>.toPlist(): PersistentList<E> = PersistentList.create(this)

operator fun <E> ISeq<E>.component1(): E = this.first()

operator fun <E> ISeq<E>.component2(): ISeq<E> = this.rest()

fun <E> vec(coll: Iterable<E>): IPersistentVector<E> =
    PersistentVector.create(coll)

@Suppress("UNCHECKED_CAST")
fun <E> vec(coll: Any?): IPersistentVector<E> = when (coll) {
    null -> PersistentVector.EmptyVector
    is ISeq<*> -> PersistentVector(coll) as IPersistentVector<E>
    is Iterable<*> -> PersistentVector.create(coll) as IPersistentVector<E>
    is Array<*> -> PersistentVector(*coll) as IPersistentVector<E>
    is ShortArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is IntArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is FloatArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is DoubleArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is LongArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is ByteArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is CharArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    is BooleanArray -> {
        PersistentVector(*coll.toTypedArray()) as IPersistentVector<E>
    }
    else -> throw IllegalArgumentException(
        "${coll::class} can't be turned into a vec."
    )
}

fun <K, V> Map<K, V>.toPmap(): IPersistentMap<K, V> =
    PersistentArrayMap.create(this)

fun <K, V> m(vararg kvs: Pair<K, V>): IPersistentMap<K, V> = when {
    kvs.isEmpty() -> PersistentArrayMap.EmptyArrayMap
    kvs.size * 2 <= HASHTABLE_THRESHOLD ->
        PersistentArrayMap.createWithCheck(*kvs)
    else -> PersistentHashMap.createWithCheck(*kvs)
}

fun <K, V> hashMap(vararg kvs: Pair<K, V>): PersistentHashMap<K, V> = when {
    kvs.isEmpty() -> PersistentHashMap.EmptyHashMap
    else -> PersistentHashMap.create(*kvs)
}

@Suppress("UNCHECKED_CAST")
fun <E> cons(x: E, coll: Any?): ISeq<E> = when (coll) {
    null -> l(x)
    is ISeq<*> -> Cons(x, coll) as ISeq<E>
    else -> Cons(x, seq<E>(coll) as ISeq<E>)
}

fun <E> consChunk(chunk: Chunk<E>, rest: ISeq<E>): ISeq<E> =
    when (chunk.count) {
        0 -> rest
        else -> ChunkedSeq(chunk, rest)
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
    cons(
        a,
        cons(
            b,
            cons(
                c,
                cons(
                    d,
                    cons(
                        e,
                        cons(f, args)
                    )
                )
            )
        )
    )
)

fun <E> hashSet(): PersistentHashSet<E> = PersistentHashSet.EmptyHashSet

fun <E> hashSet(vararg e: E): PersistentHashSet<E> =
    PersistentHashSet.create(*e)

fun <E> hashSet(seq: ISeq<E>): PersistentHashSet<E> =
    PersistentHashSet.create(seq)

fun <E> hs(): PersistentSet<E> = PersistentHashSet.EmptyHashSet

fun <E> hs(vararg e: E): PersistentSet<E> =
    PersistentHashSet.createWithCheck(*e)

fun <E> Set<E>.toPhashSet(): PersistentHashSet<E> =
    PersistentHashSet.create(this)

fun <K, V> get(map: ILookup<K, V>?, key: K, default: V? = null): V? =
    getFrom<K, V>(map, key, default)

fun <K, V> get(map: Map<K, V>?, key: K, default: V? = null): V? =
    getFrom<K, V>(map, key, default)

fun <E> get(map: PersistentSet<E>?, key: E, default: E? = null): E? =
    getFrom<E, E>(map, key, default)

fun <E> get(map: TransientSet<E>?, key: E, default: E? = null): E? =
    getFrom<E, E>(map, key, default)

@Suppress("UNCHECKED_CAST")
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
    null -> PersistentArrayMap.createWithCheck(kv)
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

operator fun <E> IPersistentVector<E>.component1(): E = this.nth(0)

operator fun <E> IPersistentVector<E>.component2(): E = this.nth(1)

operator fun <E> IPersistentVector<E>.component3(): E = this.nth(2)

operator fun <E> IPersistentVector<E>.component4(): E = this.nth(3)

operator fun <E> IPersistentVector<E>.component5(): E = this.nth(4)

operator fun <E> IPersistentVector<E>.component6(): E = this.nth(5)

operator fun <K, V> IPersistentMap<K, V>.get(key: K): V? = this.valAt(key)

fun <E> first(x: Any?): E? = when (val seq = seq<E>(x)) {
    null -> null
    else -> {
        try {
            seq.first()
        } catch (e: NoSuchElementException) {
            null
        }
    }
}

fun <E> lazySeq(): LazySeq<E> = LazySeq { null }

/**
 * @return an instance of [LazySeq]
 *
 * @throws IllegalArgumentException if x cannot be an ISeq.
 */
inline fun <E> lazySeq(crossinline body: () -> Any?): LazySeq<E> = LazySeq {
    body()
}

fun <E> nextChunks(chunk: IChunkedSeq<E>): ISeq<E>? {
    return when (val rs = chunk.restChunks()) {
        is Empty -> null
        else -> rs
    }
}

internal fun spread(arglist: Any?): ISeq<Any?>? {
    val s = seq<Any?>(arglist)
    return when {
        s == null -> null
        s.next() == null -> seq(s.first())
        else -> cons(s.first(), spread(s.next()))
    }
}

fun <T> isEvery(pred: (T) -> Boolean, coll: Any?): Boolean {
    val s = seq<Any?>(coll) ?: return true

    val first = s.first()

    if (first != null && pred(first as T)) return isEvery(pred, s.next())

    return false
}

fun <T> conj(coll: IPersistentCollection<T>?, x: T): IPersistentCollection<T> {
    return when (coll) {
        null -> l(x)
        else -> coll.conj(x)
    }
}

fun <T> conj(
    coll: IPersistentCollection<T>?,
    x: T,
    vararg xs: T
): IPersistentCollection<T> {
    tailrec fun conj(
        coll: IPersistentCollection<T>,
        s: ISeq<T>?
    ): IPersistentCollection<T> = when (s) {
        null -> coll
        else -> conj(coll.conj(s.first()), s.next())
    }

    return conj(coll?.conj(x) ?: l(x), seq(xs))
}

fun <E> concat(): LazySeq<E> = lazySeq()

fun <E> concat(x: Any?): LazySeq<E> = lazySeq { x }

fun <E> concat(x: Any?, y: Any?): LazySeq<E> = lazySeq {
    when (val s = seq<E>(x)) {
        null -> y
        else -> when (s) {
            is IChunkedSeq<*> -> {
                consChunk(s.firstChunk(), concat(nextChunks(s), y))
            }
            else -> cons(s.first(), concat<E>(s.next(), y))
        }
    }
}

fun <E> concat(x: Any?, y: Any?, vararg zs: Any?): LazySeq<E> {
    fun cat(xy: Any?, zzs: Any?): LazySeq<E> = lazySeq {
        val xys = seq<E>(xy)
        when {
            xys === null -> when (val argsSeq = seq<E>(zzs)) {
                null -> null
                else -> cat(argsSeq.first(), argsSeq.rest())
            }
            else -> when (xys) {
                is IChunkedSeq<*> -> {
                    consChunk(xys.firstChunk(), cat(xys.restChunks(), zzs))
                }
                else -> cons(xys.first(), cat(xys.rest(), zzs))
            }
        }
    }

    return cat(concat<E>(x, y), zs)
}

fun <E> q(): PersistentQueue<E> = PersistentQueue()

fun <E> q(coll: Any?): PersistentQueue<E> {
    var s = seq<E>(coll)
    var q = q<E>()

    if (s == null) return q

    while (s != null && s !is Empty) {
        q = q.conj(s.first())
        s = s.next()
    }
    
    return q
}
