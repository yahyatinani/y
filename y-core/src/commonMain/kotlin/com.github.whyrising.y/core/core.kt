package com.github.whyrising.y.core

import com.github.whyrising.y.core.collections.ASeq
import com.github.whyrising.y.core.collections.ArrayChunk
import com.github.whyrising.y.core.collections.ArraySeq
import com.github.whyrising.y.core.collections.Associative
import com.github.whyrising.y.core.collections.Chunk
import com.github.whyrising.y.core.collections.ChunkedSeq
import com.github.whyrising.y.core.collections.Cons
import com.github.whyrising.y.core.collections.IChunkedSeq
import com.github.whyrising.y.core.collections.ILookup
import com.github.whyrising.y.core.collections.IMapEntry
import com.github.whyrising.y.core.collections.IPersistentCollection
import com.github.whyrising.y.core.collections.IPersistentMap
import com.github.whyrising.y.core.collections.IPersistentVector
import com.github.whyrising.y.core.collections.ISeq
import com.github.whyrising.y.core.collections.LazySeq
import com.github.whyrising.y.core.collections.MapEntry
import com.github.whyrising.y.core.collections.PersistentArrayMap
import com.github.whyrising.y.core.collections.PersistentHashMap
import com.github.whyrising.y.core.collections.PersistentHashSet
import com.github.whyrising.y.core.collections.PersistentList
import com.github.whyrising.y.core.collections.PersistentList.Empty
import com.github.whyrising.y.core.collections.PersistentQueue
import com.github.whyrising.y.core.collections.PersistentSet
import com.github.whyrising.y.core.collections.PersistentVector
import com.github.whyrising.y.core.collections.Seqable
import com.github.whyrising.y.core.collections.StringSeq
import com.github.whyrising.y.core.collections.TransientSet
import com.github.whyrising.y.core.util.lazyChunkedSeq
import kotlin.jvm.JvmName
import kotlin.reflect.KFunction

fun <T> identity(x: T): T = x

fun str(): String = ""

fun <T> str(x: T): String = when (x) {
  null -> ""
  else -> x.toString()
}

fun <T1, T2> str(x: T1, y: T2): String = "${str(x)}${str(y)}"

fun <T1, T2, T3> str(x: T1, y: T2, z: T3): String = "${str(x, y)}${str(z)}"

fun <T1, T2, T3, T> str(x: T1, y: T2, z: T3, vararg args: T): String =
  args.fold(str(x, y, z)) { acc, arg ->
    "$acc${str(arg)}"
  }

inline fun <T1, T2, R> curry(
  crossinline f: (T1, T2) -> R,
): (T1) -> (T2) -> R = { t1: T1 ->
  { t2: T2 ->
    f(t1, t2)
  }
}

inline fun <T1, T2, T3, R> curry(
  crossinline f: (T1, T2, T3) -> R,
): (T1) -> (T2) -> (T3) -> R =
  { t1: T1 ->
    { t2: T2 ->
      { t3: T3 -> f(t1, t2, t3) }
    }
  }

inline fun <T1, T2, T3, T4, R> curry(
  crossinline f: (T1, T2, T3, T4) -> R,
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
  crossinline f: (T1, T2, T3, T4, T5) -> R,
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
  crossinline f: (T1, T2, T3, T4, T5, T6) -> R,
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
  crossinline f: (T1) -> (T2) -> Boolean,
): (T1) -> (T2) -> Boolean = { t1: T1 -> { t2: T2 -> !f(t1)(t2) } }

@JvmName("complementY1")
inline fun <T1, T2, T3> complement(
  crossinline f: (T1) -> (T2) -> (T3) -> Boolean,
): (T1) -> (T2) -> (T3) -> Boolean = { t1: T1 ->
  { t2: T2 ->
    { t3: T3 ->
      !f(t1)(t2)(t3)
    }
  }
}

@JvmName("complementY2")
inline fun <T1, T2, T3, T4> complement(
  crossinline f: (T1) -> (T2) -> (T3) -> (T4) -> Boolean,
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
  crossinline g: () -> R2,
): () -> R = { f(g()) }

inline fun <T1, R2, R> compose(
  crossinline f: (R2) -> R,
  crossinline g: (T1) -> R2,
): (T1) -> R = { t1: T1 -> f(g(t1)) }

@JvmName("composeY1")
inline fun <T1, T2, R2, R> compose(
  crossinline f: (R2) -> R,
  crossinline g: (T1) -> (T2) -> R2,
): (T1) -> (T2) -> R = { t1: T1 -> { t2: T2 -> f(g(t1)(t2)) } }

@JvmName("composeY2")
inline fun <T1, T2, T3, R2, R> compose(
  crossinline f: (R2) -> R,
  crossinline g: (T1) -> (T2) -> (T3) -> R2,
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
fun seq(coll: Any?): ISeq<Any?>? {
  val s = when (coll) {
    null -> null
    is ASeq<*> -> coll
    is LazySeq<*> -> coll.seq()

    is Seqable<*> -> coll.seq()

    is Iterable<*> -> lazyChunkedSeq(coll.iterator())
    is Sequence<*> -> lazyChunkedSeq(coll.iterator())
    is ShortArray -> ArraySeq(coll)
    is IntArray -> ArraySeq(coll)
    is FloatArray -> ArraySeq(coll)
    is DoubleArray -> ArraySeq(coll)
    is LongArray -> ArraySeq(coll)
    is ByteArray -> ArraySeq(coll)
    is CharArray -> ArraySeq(coll)
    is BooleanArray -> ArraySeq(coll)
    is Array<*> -> ArraySeq(coll)
    is CharSequence -> StringSeq(coll)
    is Map<*, *> -> seq(coll.entries)
    else -> throw IllegalArgumentException(
      "Don't know how to create ISeq from: ${coll::class.simpleName}",
    )
  }

  return if (s == Empty) null else s
}

fun <E> Iterable<E>.seq(): ISeq<E> = seq(this) as ISeq<E>

fun <E> Array<E>.seq(): ISeq<E> = seq(this) as ISeq<E>

fun <E> Sequence<E>.seq(): ISeq<E> = seq(this) as ISeq<E>

fun CharSequence.seq(): ISeq<Char> = seq(this) as ISeq<Char>

fun ShortArray.seq(): ISeq<Short> = seq(this) as ISeq<Short>

fun IntArray.seq(): ISeq<Int> = seq(this) as ISeq<Int>

fun FloatArray.seq(): ISeq<Float> = seq(this) as ISeq<Float>

fun DoubleArray.seq(): ISeq<Double> = seq(this) as ISeq<Double>

fun LongArray.seq(): ISeq<Long> = seq(this) as ISeq<Long>

fun ByteArray.seq(): ISeq<Byte> = seq(this) as ISeq<Byte>

fun CharArray.seq(): ISeq<Char> = seq(this) as ISeq<Char>

fun BooleanArray.seq(): ISeq<Boolean> = seq(this) as ISeq<Boolean>

fun <K, V> Map<K, V>.seq(): ISeq<MapEntry<K, V>> =
  seq(this) as ISeq<MapEntry<K, V>>

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
  is ISeq<*> -> PersistentVector(coll)
  is Iterable<*> -> PersistentVector.create(coll)
  is Array<*> -> PersistentVector(*coll)
  is ShortArray -> PersistentVector(*coll.toTypedArray())
  is IntArray -> PersistentVector(*coll.toTypedArray())
  is FloatArray -> PersistentVector(*coll.toTypedArray())
  is DoubleArray -> PersistentVector(*coll.toTypedArray())
  is LongArray -> PersistentVector(*coll.toTypedArray())
  is ByteArray -> PersistentVector(*coll.toTypedArray())
  is CharArray -> PersistentVector(*coll.toTypedArray())
  is BooleanArray -> PersistentVector(*coll.toTypedArray())
  else -> throw IllegalArgumentException(
    "${coll::class.simpleName} can't be turned into a vec.",
  )
} as IPersistentVector<E>

fun <K, V> Map<K, V>.toPmap(): IPersistentMap<K, V> =
  PersistentArrayMap.create(this)

fun m(vararg kvs: Pair<Any?, Any?>): PersistentArrayMap<Any?, Any?> = when {
  kvs.isEmpty() -> PersistentArrayMap.EmptyArrayMap
  else -> PersistentArrayMap.createWithCheck(*kvs)
}

fun <K, V> hashMap(vararg kvs: Pair<K, V>): PersistentHashMap<K, V> = when {
  kvs.isEmpty() -> PersistentHashMap.EmptyHashMap
  else -> PersistentHashMap.create(*kvs)
}

@Suppress("UNCHECKED_CAST")
fun cons(x: Any?, coll: Any?): ISeq<Any?> = when (coll) {
  null -> l(x)
  is ISeq<*> -> Cons(x, coll)
  else -> Cons(x, seq(coll) ?: Empty)
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

@Suppress("UNCHECKED_CAST")
fun <V> get(map: Any?, key: Any?, default: V? = null): V? = when (map) {
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

  else -> null
}

fun <K, V> assoc(
  map: Associative<K, V>?,
  kv: Pair<K, V>,
): Associative<K, V> = when (map) {
  null -> PersistentArrayMap.createWithCheck(kv)
  else -> map.assoc(kv.first, kv.second)
}

@Suppress("UNCHECKED_CAST")
tailrec fun <K, V> assoc(
  map: Associative<K, V>?,
  kv: Pair<K, V>,
  vararg kvs: Pair<K, V>,
): Associative<K, V> {
  val m = assoc(map, kv)
  return when {
    kvs.isNotEmpty() -> {
      val rest = kvs.copyInto(
        arrayOfNulls(kvs.size - 1),
        0,
        1,
        kvs.size,
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
  v: V,
): Associative<K, V> = ks.let { (k, kz) ->
  when {
    ks.count > 1 -> {
      val m = assocIn(
        get<Associative<K, V>>(map, k),
        kz,
        v,
      )
      assoc(map, k to m) as Associative<K, V>
    }

    else -> assoc(map, k to v)
  }
}

fun <V> getIn(m: Any?, ks: ISeq<*>, default: V? = null): V? {
  val token = Any()
  tailrec fun getIn(m: Any?, kz: ISeq<*>): V? {
    return when {
      kz.count > 0 -> {
        val mm = get(m, kz.first(), token)
        when {
          mm === token -> default
          else -> getIn(mm, kz.rest())
        }
      }

      else -> m as V?
    }
  }
  return getIn(m, ks)
}

operator fun <E> IPersistentVector<E>.component1(): E = this.nth(0)

operator fun <E> IPersistentVector<E>.component2(): E = this.nth(1)

operator fun <E> IPersistentVector<E>.component3(): E = this.nth(2)

operator fun <E> IPersistentVector<E>.component4(): E = this.nth(3)

operator fun <E> IPersistentVector<E>.component5(): E = this.nth(4)

operator fun <E> IPersistentVector<E>.component6(): E = this.nth(5)

operator fun <K, V> Associative<K, V>.get(key: K): V? = this.valAt(key)

fun <E> first(x: Any?): E? = when (val seq = seq(x)) {
  null -> null
  else -> when (seq.count) {
    0 -> null
    else -> seq.first() as E?
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

@Suppress("UNCHECKED_CAST")
fun <T> isEvery(pred: (T) -> Boolean, coll: Any?): Boolean {
  val s = seq(coll) ?: return true

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

fun conj(
  coll: IPersistentCollection<Any?>?,
  x: Any?,
  vararg xs: Any?,
): IPersistentCollection<Any?> {
  tailrec fun conj(
    coll: IPersistentCollection<Any?>,
    s: ISeq<Any?>?,
  ): IPersistentCollection<Any?> = when (s) {
    null -> coll
    else -> conj(coll.conj(s.first()), s.next())
  }

  return conj(coll?.conj(x) ?: l(x), seq(xs))
}

fun <E> concat(): LazySeq<E> = lazySeq()

fun <E> concat(x: Any?): LazySeq<E> = lazySeq { x }

fun <E> concat(x: Any?, y: Any?): LazySeq<E> = lazySeq {
  when (val s = seq(x)) {
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
    val xys = seq(xy)
    when {
      xys === null -> when (val argsSeq = seq(zzs)) {
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

fun q(): PersistentQueue<Any?> = PersistentQueue()

fun q(coll: Any?): PersistentQueue<Any?> {
  var s = seq(coll)
  var q = q()
  if (s == null) return q

  while (s != null) {
    q = q.conj(s.first())
    s = s.next()
  }

  return q
}

fun s(name: String): Symbol = Symbol(name)

// -- spread -------------------------------------------------------------------
fun spread(arglist: Any?): ISeq<Any?>? {
  val s = seq(arglist)
  return when {
    s == null -> null
    s.next() == null -> seq(s.first())
    else -> cons(s.first(), spread(s.next()))
  }
}

// -- prepend ------------------------------------------------------------------

/**
 * @param args should be a sequence or sequence compatible.
 * @return a new [ISeq] containing the items prepended to the rest.
 */
fun prepend(args: Any?): ISeq<Any?>? = seq(args)

fun prepend(a: Any?, args: Any?): ISeq<Any?> = cons(a, args)

fun prepend(a: Any?, b: Any?, args: Any?): ISeq<Any?> = cons(a, cons(b, args))

fun prepend(a: Any?, b: Any?, c: Any?, args: Any?): ISeq<Any?> =
  cons(a, cons(b, cons(c, args)))

fun prepend(
  a: Any?,
  b: Any?,
  c: Any?,
  d: Any?,
  vararg more: Any?,
): ISeq<Any?> = cons(a, cons(b, cons(c, cons(d, spread(more)))))

// -- apply --------------------------------------------------------------------

data class ArityException(val n: Int?, val f: Any?) : IllegalArgumentException(
  "Wrong number of args $n passed to $f",
)

private fun f(f: Function<*>): Any = if (f is KFunction<*>) f.name else f

fun <R> apply(f: Function<R>, args: Any?): R {
  var argsSeq = seq(args)
  return when (val arity: Int = argsSeq?.count ?: 0) {
    0 -> (f as? Function0<R> ?: throw ArityException(arity, f(f))).invoke()

    1 -> {
      (f as? Function1<Any?, R> ?: throw ArityException(arity, f(f)))
        .invoke(argsSeq!!.first())
    }

    2 -> {
      (f as? Function2<Any?, Any?, R> ?: throw ArityException(arity, f(f)))
        .invoke(
          argsSeq!!.first(),
          (argsSeq.next().also { argsSeq = it })!!.first(),
        )
    }

    3 -> (
      f as? Function3<Any?, Any?, Any?, R> ?: throw ArityException(
        arity,
        f(f),
      )
      )
      .invoke(
        argsSeq!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
      )

    4 -> {
      (
        f as? Function4<Any?, Any?, Any?, Any?, R> ?: throw ArityException(
          arity,
          f(f),
        )
        )
        .invoke(
          argsSeq!!.first(),
          (argsSeq!!.next().also { argsSeq = it })?.first(),
          (argsSeq!!.next().also { argsSeq = it })?.first(),
          (argsSeq!!.next().also { argsSeq = it })?.first(),
        )
    }

    5 -> (
      f as? Function5<Any?, Any?, Any?, Any?, Any?, R>
        ?: throw ArityException(arity, f(f))
      )
      .invoke(
        argsSeq!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
      )

    6 -> (
      f as? Function6<Any?, Any?, Any?, Any?, Any?, Any?, R>
        ?: throw ArityException(arity, f(f))
      )
      .invoke(
        argsSeq!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
      )

    7 -> (
      f as? Function7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>
        ?: throw ArityException(arity, f(f))
      )
      .invoke(
        argsSeq?.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
        (argsSeq!!.next().also { argsSeq = it })!!.first(),
      )

    else -> TODO("apply() supports a maximum arity of 7 for now")
  }
}

fun <R> apply(f: Function<R>, x: Any?, args: Any?): R =
  apply(f, prepend(x, args))

fun <R> apply(f: Function<R>, x: Any?, y: Any?, args: Any?): R =
  apply(f, prepend(x, y, args))

fun <R> apply(f: Function<R>, x: Any?, y: Any?, z: Any?, args: Any?): R =
  apply(f, prepend(x, y, z, args))

fun <R> apply(
  f: Function<R>,
  a: Any?,
  b: Any?,
  c: Any?,
  d: Any?,
  vararg args: Any?,
): R = apply(f, cons(a, cons(b, cons(c, cons(d, spread(args))))))

// -- update(m, k, f) ----------------------------------------------------------

fun update(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: Function<Any?>,
): Associative<Any?, Any?> = when (f) {
  is KFunction<Any?> -> {
    assoc(m, k to (f as Function1<Any?, Any?>).invoke(get(m, k)))
  }

  else -> assoc(m, k to (f as (oldVal: Any?) -> Any?)(get(m, k)))
}

fun update(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: Function<Any?>,
  x: Any?,
): Associative<Any?, Any?> = when (f) {
  is KFunction<Any?> -> {
    assoc(m, k to (f as Function2<Any?, Any?, Any?>)(get(m, k), x))
  }

  else -> assoc(m, k to (f as (Any?, Any?) -> Any?)(get(m, k), x))
}

fun update(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: Function<Any?>,
  x: Any?,
  y: Any?,
): Associative<Any?, Any?> = when (f) {
  is KFunction<Any?> -> {
    assoc(m, k to (f as Function3<Any?, Any?, Any?, Any?>)(get(m, k), x, y))
  }

  else -> assoc(m, k to (f as (Any?, Any?, Any?) -> Any?)(get(m, k), x, y))
}

fun update(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: Function<Any?>,
  x: Any?,
  y: Any?,
  z: Any?,
): Associative<Any?, Any?> = when (f) {
  is KFunction<Any?> -> assoc(
    m,
    k to (f as Function4<Any?, Any?, Any?, Any?, Any?>)(get(m, k), x, y, z),
  )

  else -> assoc(
    m,
    k to (f as (Any?, Any?, Any?, Any?) -> Any?)(get(m, k), x, y, z),
  )
}

fun update(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: Function<Any?>,
  x: Any?,
  y: Any?,
  z: Any?,
  vararg more: Any?,
): Associative<Any?, Any?> = assoc(m, k to apply(f, get(m, k), x, y, z, more))

// -- updateIn() ---------------------------------------------------------------
fun updateIn(
  m: Any?,
  ks: ISeq<Any>,
  f: Function<Any?>,
  vararg args: Any?,
): Associative<Any?, Any?> {
  fun upIn(
    map: Associative<Any?, Any?>?,
    ks: ISeq<Any>,
    f: Function<Any?>,
  ): Associative<Any?, Any?> {
    val (k, nks) = ks
    return when (nks.count) {
      0 -> assoc(
        map,
        k to apply(f, get(map, k), if (args.isEmpty()) null else args),
      )

      else -> assoc(map, k to upIn(get(map, k), nks, f))
    }
  }

  return upIn((m as Associative<Any?, Any?>?), ks, f)
}

// -- map ----------------------------------------------------------------------
inline fun <T> chunkBuffer(capacity: Int, end: Int, f: (index: Int) -> T):
  Array<Any?> {
  val buffer = arrayOfNulls<Any?>(capacity)
  for (i in 0 until end) buffer[i] = f(i)
  return buffer
}

fun map(f: Function<Any?>, coll: Any?): LazySeq<Any?> = lazySeq {
  val function = f as (Any?) -> Any?
  when (val seq = seq(coll)) {
    null -> null

    is IChunkedSeq<*> -> {
      val firstChunk = seq.firstChunk()
      val count = firstChunk.count
      val buffer = chunkBuffer(capacity = count, end = count) { index ->
        function(firstChunk.nth(index))
      }
      consChunk(ArrayChunk(buffer), map(f, seq.restChunks()))
    }

    else -> cons(function(seq.first()), map(f, seq.rest()))
  }
}

fun map(f: Function<Any?>, coll1: Any?, coll2: Any?): LazySeq<Any?> = lazySeq {
  val s1 = seq(coll1)
  val s2 = seq(coll2)
  if (s1 == null || s2 == null) return@lazySeq null

  cons(
    x = (f as (Any?, Any?) -> Any?)(s1.first(), s2.first()),
    coll = map(f, s1.rest(), s2.rest()),
  )
}

fun map(
  f: Function<Any?>,
  coll1: Any?,
  coll2: Any?,
  coll3: Any?,
): LazySeq<Any?> = lazySeq {
  val s1 = seq(coll1)
  val s2 = seq(coll2)
  val s3 = seq(coll3)
  if (s1 == null || s2 == null || s3 == null) return@lazySeq null

  cons(
    x = (f as (Any?, Any?, Any?) -> Any?)(s1.first(), s2.first(), s3.first()),
    coll = map(f, s1.rest(), s2.rest(), s3.rest()),
  )
}

fun map(
  f: Function<Any?>,
  coll1: Any?,
  coll2: Any?,
  coll3: Any?,
  vararg colls: Any?,
): LazySeq<Any?> = lazySeq {
  fun step(cs: Any?): LazySeq<Any?> = lazySeq {
    val ss = map(::seq, cs)

    var sss: ISeq<Any?>? = ss
    while (sss != null) {
      sss.first() ?: return@lazySeq null
      sss = sss.next()
    }

    cons(map(::first, ss), step(map(ISeq<Any?>::next, ss)))
  }

  map({ e: Any? -> apply(f, e) }, step(conj(seq(colls), coll3, coll2, coll1)))
}

// -- merge --------------------------------------------------------------------

/**
 * @param maps First map should be of type [IPersistentMap], the rest can be of
 * type [Map] or [IPersistentMap].
 */
fun merge(vararg maps: Any?): IPersistentMap<Any, Any?>? {
  if (maps.firstNotNullOfOrNull { it } == null) return null

  return maps.reduce { acc, map ->
    conj((acc as IPersistentCollection<Any?>?) ?: m(), map)
  } as IPersistentMap<Any, Any?>?
}

// -- selectKeys ---------------------------------------------------------------
fun find(map: Any?, key: Any?): IMapEntry<Any?, Any?>? = when (map) {
  null -> null
  is Associative<*, *> -> map.entryAt(key)
  is Map<*, *> -> if (map.contains(key)) MapEntry(key, map[key]) else null

  else -> throw IllegalArgumentException(
    "find not supported on type: ${map::class.simpleName}",
  )
}

fun selectKeys(
  map: Any?,
  keySeq: ISeq<Any?>,
): Associative<Any?, Any?> {
  tailrec fun selectKeys(
    ret: IPersistentCollection<Any?>,
    keySeq: ISeq<Any?>?,
  ): IPersistentCollection<Any?> {
    if (keySeq == null) return ret

    val entry = find(map, keySeq.first())
    return selectKeys(
      if (entry != null) conj(ret, entry) else ret,
      keySeq.next(),
    )
  }

  return selectKeys(m(), keySeq) as Associative<Any?, Any?>
}

// -- ISeq<T>::reduce() --------------------------------------------------------
inline fun <S, T : S> ISeq<T>.reduce(operation: (acc: S, T) -> S): S {
  if (count == 0) {
    throw UnsupportedOperationException("Empty sequence can't be reduced.")
  }

  var accumulator: S = this.first()
  var s: ISeq<T>? = this.next()
  while (s != null) {
    accumulator = operation(accumulator, s.first())
    s = s.next()
  }
  return accumulator
}

// -- ISeq<T>::fold() ----------------------------------------------------------
inline fun <T, R> ISeq<T>.fold(initial: R, operation: (acc: R, T) -> R): R {
  var s: ISeq<T>? = this
  var accumulator = initial
  while (s != null) {
    accumulator = operation(accumulator, s.first())
    s = s.next()
  }
  return accumulator
}

// -- into() -------------------------------------------------------------------
fun <T> into(to: T, from: Any?): T? = seq(from)?.fold(to) { acc, any ->
  conj(acc as IPersistentCollection<Any>?, any) as T
}
