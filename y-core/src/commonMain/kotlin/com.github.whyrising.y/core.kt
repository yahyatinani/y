package com.github.whyrising.y

import com.github.whyrising.y.collections.ArrayChunk
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
import com.github.whyrising.y.collections.vector.IPersistentVector
import com.github.whyrising.y.util.lazyChunkedSeq
import kotlin.jvm.JvmName

fun <T> identity(x: T): T = x

class NotANumberError(x: Any) : RuntimeException(
    "Either `$x` is not a number or this type is not supported."
)

@Suppress("UNCHECKED_CAST")
fun <T : Number> inc(x: T): T = when (x) {
    is Byte -> x.inc() as T
    is Short -> x.inc() as T
    is Int -> x.inc() as T
    is Long -> x.inc() as T
    is Float -> x.inc() as T
    is Double -> x.inc() as T
    // TODO is BigInteger -> x.inc() as T
    else -> throw NotANumberError(x)
}

@Suppress("UNCHECKED_CAST")
fun <T : Number> dec(x: T): T = when (x) {
    is Byte -> x.dec() as T
    is Short -> x.dec() as T
    is Int -> x.dec() as T
    is Long -> x.dec() as T
    is Float -> x.dec() as T
    is Double -> x.dec() as T
// TODO is BigInteger -> x.dec() as T
    else -> throw NotANumberError(x)
}

fun str(): String = ""

fun <T> str(x: T): String = when (x) {
    null -> ""
    else -> x.toString()
}

fun <T1, T2> str(x: T1, y: T2): String = "${str(x)}${str(y)}"

fun <T1, T2, T3> str(x: T1, y: T2, z: T3): String = "${str(x, y)}${str(z)}"

fun <T1, T2, T3, T> str(x: T1, y: T2, z: T3, vararg args: T): String =
    args.fold("") { acc, arg ->
        "$acc${str(arg)}"
    }.let { "${str(x, y, z)}$it" }

inline fun <T1, T2, R> curry(
    crossinline f: (T1, T2) -> R
): (T1) -> (T2) -> R = { t1: T1 ->
    { t2: T2 ->
        f(t1, t2)
    }
}

inline fun <T1, T2, T3, R> curry(
    crossinline f: (T1, T2, T3) -> R
): (T1) -> (T2) -> (T3) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 -> f(t1, t2, t3) }
        }
    }

inline fun <T1, T2, T3, T4, R> curry(
    crossinline f: (T1, T2, T3, T4) -> R
): (T1) -> (T2) -> (T3) -> (T4) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 ->
                { t4: T4 ->
                    f(t1, t2, t3, t4)
                }
            }
        }
    }

inline fun <T1, T2, T3, T4, T5, R> curry(
    crossinline f: (T1, T2, T3, T4, T5) -> R
): (T1) -> (T2) -> (T3) -> (T4) -> (T5) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 ->
                { t4: T4 ->
                    { t5: T5 ->
                        f(t1, t2, t3, t4, t5)
                    }
                }
            }
        }
    }

inline fun <T1, T2, T3, T4, T5, T6, R> curry(
    crossinline f: (T1, T2, T3, T4, T5, T6) -> R
): (T1) -> (T2) -> (T3) -> (T4) -> (T5) -> (T6) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 ->
                { t4: T4 ->
                    { t5: T5 ->
                        { t6: T6 -> f(t1, t2, t3, t4, t5, t6) }
                    }
                }
            }
        }
    }

inline fun complement(crossinline f: () -> Boolean): () -> Boolean = { !f() }

inline fun <T> complement(crossinline f: (T) -> Boolean): (T) -> Boolean =
    { t: T -> !f(t) }

@JvmName("complementY")
inline fun <T1, T2> complement(
    crossinline f: (T1) -> (T2) -> Boolean
): (T1) -> (T2) -> Boolean = { t1: T1 -> { t2: T2 -> !f(t1)(t2) } }

@JvmName("complementY1")
inline fun <T1, T2, T3> complement(
    crossinline f: (T1) -> (T2) -> (T3) -> Boolean
): (T1) -> (T2) -> (T3) -> Boolean = { t1: T1 ->
    { t2: T2 ->
        { t3: T3 ->
            !f(t1)(t2)(t3)
        }
    }
}

@JvmName("complementY2")
inline fun <T1, T2, T3, T4> complement(
    crossinline f: (T1) -> (T2) -> (T3) -> (T4) -> Boolean
): (T1) -> (T2) -> (T3) -> (T4) -> Boolean = { t1: T1 ->
    { t2: T2 ->
        { t3: T3 ->
            { t4: T4 -> !f(t1)(t2)(t3)(t4) }
        }
    }
}

fun <T> compose(): (T) -> T = ::identity

fun <T> compose(f: T): T = f

inline fun <R2, R> compose(
    crossinline f: (R2) -> R,
    crossinline g: () -> R2
): () -> R = { f(g()) }

inline fun <T1, R2, R> compose(
    crossinline f: (R2) -> R,
    crossinline g: (T1) -> R2
): (T1) -> R = { t1: T1 -> f(g(t1)) }

@JvmName("composeY1")
inline fun <T1, T2, R2, R> compose(
    crossinline f: (R2) -> R,
    crossinline g: (T1) -> (T2) -> R2
): (T1) -> (T2) -> R = { t1: T1 -> { t2: T2 -> f(g(t1)(t2)) } }

@JvmName("composeY2")
inline fun <T1, T2, T3, R2, R> compose(
    crossinline f: (R2) -> R,
    crossinline g: (T1) -> (T2) -> (T3) -> R2
): (T1) -> (T2) -> (T3) -> R = { t1: T1 ->
    { t2: T2 ->
        { t3: T3 ->
            f(g(t1)(t2)(t3))
        }
    }
}

/**
 * @param coll should be an [Iterable] or a [Seqable] of elements of the given
 * type [E].
 *
 * @return an [ISeq] on the given [coll]. If [coll] is null or empty,
 * it returns null.
 * @throws IllegalArgumentException if [coll] is not sequable.
 */
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

fun <K, V> m(vararg kvs: Pair<K, V>): PersistentArrayMap<K, V> = when {
    kvs.isEmpty() -> PersistentArrayMap.EmptyArrayMap
    else -> PersistentArrayMap.createWithCheck(*kvs)
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

fun <E> consChunk(chunk: Chunk<E>, rest: ISeq<E>) = when (chunk.count) {
    0 -> rest
    else -> ChunkedSeq(chunk, rest)
}

fun <E> v(): PersistentVector<E> = PersistentVector()

fun <E> v(a: E): PersistentVector<E> = PersistentVector(a)

fun <E> v(a: E, b: E): PersistentVector<E> = PersistentVector(a, b)

fun <E> v(a: E, b: E, c: E): PersistentVector<E> = PersistentVector(a, b, c)

fun <E> v(a: E, b: E, c: E, d: E) = PersistentVector(a, b, c, d)

fun <E> v(a: E, b: E, c: E, d: E, e: E) = PersistentVector(a, b, c, d, e)

fun <E> v(a: E, b: E, c: E, d: E, e: E, f: E): PersistentVector<E> =
    PersistentVector(a, b, c, d, e, f)

fun <E> v(a: E, b: E, c: E, d: E, e: E, f: E, vararg args: E) =
    PersistentVector(cons(a, cons(b, cons(c, cons(d, cons(e, cons(f, args)))))))

fun <E> hashSet(): PersistentHashSet<E> = PersistentHashSet.EmptyHashSet

fun <E> hashSet(vararg e: E) = PersistentHashSet.create(*e)

fun <E> hashSet(seq: ISeq<E>) = PersistentHashSet.create(seq)

fun <E> hs(): PersistentSet<E> = PersistentHashSet.EmptyHashSet

fun <E> hs(vararg e: E) = PersistentHashSet.createWithCheck(*e)

fun <E> Set<E>.toPhashSet() = PersistentHashSet.create(this)

fun <K, V> get(map: ILookup<K, V>?, key: K, default: V? = null): V? =
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

@Suppress("UNCHECKED_CAST")
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

internal fun <T> chunkBuffer(capacity: Int, end: Int, f: (index: Int) -> T):
    Array<Any?> {
    val buffer = arrayOfNulls<Any?>(capacity)
    for (i in 0 until end)
        buffer[i] = f(i)
    return buffer
}

/**
 * @param coll should be an [Iterable] or a [Seqable] of elements of type [T].
 * @param f that takes the elements of [coll] as arguments.
 * @return a [LazySeq] consisting of the result of applying [f] to each element
 * in the given [coll]. */
@Suppress("UNCHECKED_CAST")
fun <T, R> map(coll: Any?, f: (T) -> R): LazySeq<R> = lazySeq {
    when (val seq = seq<T>(coll)) {
        null -> null
        is IChunkedSeq<*> -> {
            seq as IChunkedSeq<T>
            val firstChunk = seq.firstChunk()
            val count = firstChunk.count
            val buffer = chunkBuffer(capacity = count, end = count) { index ->
                f(firstChunk.nth(index))
            }
            consChunk(ArrayChunk(buffer), map(seq.restChunks(), f))
        }
        else -> cons(f(seq.first()), map(seq.rest(), f))
    }
}

/**
 * @param c1 should be an [Iterable] or a [Seqable] of elements of type [T1].
 * @param c2 should be an [Iterable] or a [Seqable] of elements of type [T2].
 * @param f takes 1st argument form [c1] and the 2nd from [c2].
 * @return a [LazySeq] consisting of the result of applying [f] to the set of
 * first items of [c1] and [c2], followed by applying [f] to the set of second
 * items in [c1] and [c2], until one or both of the collections are exhausted.
 * If the collections didn't have the same size, the remaining items in either
 * of them are ignored.
 */
fun <T1, T2, R> map(c1: Any?, c2: Any?, f: (T1, T2) -> R): LazySeq<R> =
    lazySeq {
        val seq1 = seq<T1>(c1)
        val seq2 = seq<T2>(c2)
        if (seq1 == null || seq2 == null) return@lazySeq null

        cons(f(seq1.first(), seq2.first()), map(seq1.rest(), seq2.rest(), f))
    }

/**
 * @param c1 should be an [Iterable] or a [Seqable] of elements of type [T1].
 * @param c2 should be an [Iterable] or a [Seqable] of elements of type [T2].
 * @param c3 should be an [Iterable] or a [Seqable] of elements of type [T3].
 * @param f takes 1st argument form [c1] and the 2nd from [c2] and 3rd from [c3]
 * @return a [LazySeq] consisting of the result of applying [f] to the set of
 * first items of [c1], [c2], and [c3], followed by applying [f] to the set of
 * second items in [c1], [c2], and [c3], until one or all of the collections
 * are exhausted.
 * If the collections didn't have the same size, the remaining items in either
 * of them are ignored.
 */
fun <T1, T2, T3, R> map(
    c1: Any?,
    c2: Any?,
    c3: Any?,
    f: (T1, T2, T3) -> R
): LazySeq<R> = lazySeq {
    val seq1 = seq<T1>(c1)
    val seq2 = seq<T2>(c2)
    val seq3 = seq<T3>(c3)
    if (seq1 == null || seq2 == null || seq3 == null) return@lazySeq null
    cons(
        f(seq1.first(), seq2.first(), seq3.first()),
        map(seq1.rest(), seq2.rest(), seq3.rest(), f)
    )
}
