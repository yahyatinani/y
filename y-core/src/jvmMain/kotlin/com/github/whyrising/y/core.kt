package com.github.whyrising.y

import com.github.whyrising.y.core.ArityException
import com.github.whyrising.y.core.apply
import com.github.whyrising.y.core.assoc
import com.github.whyrising.y.core.chunkBuffer
import com.github.whyrising.y.core.collections.ArrayChunk
import com.github.whyrising.y.core.collections.Associative
import com.github.whyrising.y.core.collections.IChunkedSeq
import com.github.whyrising.y.core.collections.ISeq
import com.github.whyrising.y.core.collections.LazySeq
import com.github.whyrising.y.core.component1
import com.github.whyrising.y.core.component2
import com.github.whyrising.y.core.concat
import com.github.whyrising.y.core.conj
import com.github.whyrising.y.core.cons
import com.github.whyrising.y.core.consChunk
import com.github.whyrising.y.core.first
import com.github.whyrising.y.core.get
import com.github.whyrising.y.core.l
import com.github.whyrising.y.core.lazySeq
import com.github.whyrising.y.core.map
import com.github.whyrising.y.core.prepend
import com.github.whyrising.y.core.seq
import com.github.whyrising.y.core.spread
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4
import kotlin.reflect.KFunction5
import kotlin.reflect.KFunction6
import kotlin.reflect.KFunction7
import kotlin.reflect.full.valueParameters

// -- apply() ------------------------------------------------------------------
fun seqToArray(seq: ISeq<Any?>?): Array<Any?> {
  val ret = arrayOfNulls<Any>(seq?.count ?: 0)
  var i = 0
  var s = seq
  while (s != null && s.count > 0) {
    ret[i] = s.first()
    ++i
    s = s.next()
  }
  return ret
}

// FIXME: remember that not all of varargs are Array<*>, it can be IntArray...
fun <R> applyVar(f: KFunction<R>, args: Any?): R {
  var argsSeq = seq(args)
  val argsCount = argsSeq?.count ?: 0
  val requiredArity = f.valueParameters.size
  if (argsCount < requiredArity - 1) throw ArityException(argsCount, f.name)
  return when (requiredArity) {
    1 -> (f as KFunction1<Array<out Any?>, R>).invoke(seqToArray(argsSeq))
    2 -> (f as KFunction2<Any?, Array<out Any?>, R>).invoke(
      argsSeq?.first(),
      seqToArray(argsSeq?.next().also { argsSeq = it }),
    )

    3 -> (f as KFunction3<Any?, Any?, Any?, R>).invoke(
      argsSeq?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      seqToArray(argsSeq?.next().also { argsSeq = it }),
    )

    4 -> (f as KFunction4<Any?, Any?, Any?, Any?, R>).invoke(
      argsSeq?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      seqToArray(argsSeq?.next().also { argsSeq = it }),
    )

    5 -> (f as KFunction5<Any?, Any?, Any?, Any?, Any?, R>).invoke(
      argsSeq?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      seqToArray(argsSeq?.next().also { argsSeq = it }),
    )

    6 -> (f as KFunction6<Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
      argsSeq?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      (argsSeq?.next().also { argsSeq = it })?.first(),
      seqToArray(argsSeq?.next().also { argsSeq = it }),
    )

    7 -> (f as KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>)
      .invoke(
        argsSeq?.first(),
        (argsSeq?.next().also { argsSeq = it })?.first(),
        (argsSeq?.next().also { argsSeq = it })?.first(),
        (argsSeq?.next().also { argsSeq = it })?.first(),
        (argsSeq?.next().also { argsSeq = it })?.first(),
        (argsSeq?.next().also { argsSeq = it })?.first(),
        seqToArray(argsSeq?.next().also { argsSeq = it }),
      )

    else -> TODO("Arity $requiredArity not supported")
  }
}

fun <R> applyVar(f: KFunction<R>, x: Any?, args: Any?): R =
  applyVar(f, prepend(x, args))

fun <R> applyVar(f: KFunction<R>, x: Any?, y: Any?, args: Any?): R =
  applyVar(f, prepend(x, y, args))

fun <R> applyVar(f: KFunction<R>, x: Any?, y: Any?, z: Any?, args: Any?): R =
  applyVar(f, prepend(x, y, z, args))

fun <R> applyVar(
  f: KFunction<R>,
  a: Any?,
  b: Any?,
  c: Any?,
  d: Any?,
  vararg args: Any?,
): R = applyVar(f, cons(a, cons(b, cons(c, cons(d, spread(args))))))

// -- updateVar ----------------------------------------------------------------

/** @param f should be 1 or 2 args maximum function with last one as vararg . */
fun updateVar(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: KFunction<Any?>,
): Associative<Any?, Any?> = when (f.valueParameters.size) {
  1 -> assoc(
    m,
    k to (f as Function1<Any?, Any?>).invoke(arrayOf<Any?>(get(m, k))),
  )

  2 -> assoc(
    m,
    k to (f as Function2<Any?, Any?, Any?>)(get(m, k), emptyArray<Any?>()),
  )

  else -> throw ArityException(1, f.name)
}

/** @param f a function of 2/3 args maximum and last arg as vararg . */
fun updateVar(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: KFunction<Any?>,
  x: Any?,
): Associative<Any?, Any?> = when (f.valueParameters.size) {
  1 -> assoc(m, k to (f as Function1<Any?, Any?>).invoke(arrayOf(get(m, k), x)))

  2 -> assoc(
    m,
    k to (f as Function2<Any?, Any?, Any?>)(get(m, k), arrayOf(x)),
  )

  3 -> assoc(
    m,
    k to (f as Function3<Any?, Any?, Any?, Any?>)(
      get(m, k),
      x,
      emptyArray<Any?>(),
    ),
  )

  else -> throw ArityException(2, f.name)
}

/** @param f a function of 3/4 args maximum and last arg as vararg . */
fun updateVar(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: KFunction<Any?>,
  x: Any?,
  y: Any?,
): Associative<Any?, Any?> = when (f.valueParameters.size) {
  1 -> assoc(
    m,
    k to (f as Function1<Any?, Any?>).invoke(arrayOf(get(m, k), x, y)),
  )

  2 -> assoc(
    m,
    k to (f as Function2<Any?, Any?, Any?>)(get(m, k), arrayOf(x, y)),
  )

  3 -> assoc(
    m,
    k to (f as Function3<Any?, Any?, Any?, Any?>)(
      get(m, k),
      x,
      arrayOf(y),
    ),
  )

  4 -> assoc(
    m,
    k to (f as Function4<Any?, Any?, Any?, Any?, Any?>)(
      get(m, k),
      x,
      y,
      emptyArray<Any?>(),
    ),
  )

  else -> throw ArityException(3, f.name)
}

/** @param f a function of 4/5 args maximum and last arg as vararg . */
fun updateVar(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: KFunction<Any?>,
  x: Any?,
  y: Any?,
  z: Any?,
): Associative<Any?, Any?> = when (f.valueParameters.size) {
  1 -> assoc(
    m,
    k to (f as Function1<Any?, Any?>).invoke(arrayOf(get(m, k), x, y, z)),
  )

  2 -> assoc(
    m,
    k to (f as Function2<Any?, Any?, Any?>)(get(m, k), arrayOf(x, y, z)),
  )

  3 -> assoc(
    m,
    k to (f as Function3<Any?, Any?, Any?, Any?>)(
      get(m, k),
      x,
      arrayOf(y, z),
    ),
  )

  4 -> assoc(
    m,
    k to (f as Function4<Any?, Any?, Any?, Any?, Any?>)(
      get(m, k),
      x,
      y,
      arrayOf(z),
    ),
  )

  5 -> assoc(
    m,
    k to (f as Function5<Any?, Any?, Any?, Any?, Any?, Any?>)(
      get(m, k),
      x,
      y,
      z,
      emptyArray<Any?>(),
    ),
  )

  else -> throw ArityException(4, f.name)
}

/** @param f a function of 6/7 args maximum and last arg as vararg. */
fun updateVar(
  m: Associative<Any?, Any?>?,
  k: Any?,
  f: KFunction<Any?>,
  x: Any?,
  y: Any?,
  z: Any?,
  vararg more: Any?,
): Associative<Any?, Any?> =
  assoc(m, k to applyVar(f, get(m, k), x, y, z, more))

// -- updateInVar() ------------------------------------------------------------

fun updateInVar(
  m: Any?,
  ks: ISeq<Any>,
  f: KFunction<Any?>,
  vararg args: Any?,
): Associative<Any?, Any?> {
  fun upIn(
    map: Associative<Any?, Any?>?,
    ks: ISeq<Any>,
    f: KFunction<Any?>,
  ): Associative<Any?, Any?> {
    val (k, nks) = ks
    return when (nks.count) {
      0 -> assoc(map, k to applyVar(f, get(map, k), args))
      else -> assoc(map, k to upIn(get(map, k), nks, f))
    }
  }

  return upIn((m as Associative<Any?, Any?>?), ks, f)
}

fun mapVar(f: KFunction<Any?>, coll: Any?): LazySeq<Any?> = lazySeq {
  val function = f as (Array<out Any?>) -> Any?

  when (val seq = seq(coll)) {
    null -> null

    is IChunkedSeq<*> -> {
      val firstChunk = seq.firstChunk()
      val count = firstChunk.count
      val buffer = chunkBuffer(capacity = count, end = count) { index ->
        function(arrayOf(firstChunk.nth(index)))
      }
      consChunk(ArrayChunk(buffer), mapVar(f, seq.restChunks()))
    }

    else -> cons(applyVar(f, l(seq.first())), mapVar(f, seq.rest()))
  }
}

fun mapVar(f: KFunction<Any?>, coll1: Any?, coll2: Any?): LazySeq<Any?> =
  lazySeq {
    val s1 = seq(coll1)
    val s2 = seq(coll2)
    if (s1 == null || s2 == null) return@lazySeq null

    cons(
      x = applyVar(f, l(s1.first(), s2.first())),
      coll = mapVar(f, s1.rest(), s2.rest()),
    )
  }

fun mapVar(
  f: KFunction<Any?>,
  coll1: Any?,
  coll2: Any?,
  coll3: Any?,
): LazySeq<Any?> = lazySeq {
  val s1 = seq(coll1)
  val s2 = seq(coll2)
  val s3 = seq(coll3)
  if (s1 == null || s2 == null || s3 == null) return@lazySeq null

  cons(
    x = applyVar(f, l(s1.first(), s2.first(), s3.first())),
    coll = mapVar(f, s1.rest(), s2.rest(), s3.rest()),
  )
}

fun mapVar(
  f: KFunction<Any?>,
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

  map(
    { e: Any? -> applyVar(f, e) },
    step(conj(seq(colls), coll3, coll2, coll1)),
  )
}

// -- mapcat -------------------------------------------------------------------

private fun getConcat(args: LazySeq<Any?>) = when (args.count) {
  1 -> {
    val c1: KFunction1<Any?, LazySeq<Any?>> = ::concat
    c1
  }

  2 -> {
    val c2: KFunction2<Any?, Any?, LazySeq<Any?>> = ::concat
    c2
  }

  else -> {
    val c3: KFunction3<Any?, Any?, Array<out Any?>, LazySeq<Any?>> = ::concat
    c3
  }
}

fun mapcat(f: Function<Any?>, vararg colls: Any?): LazySeq<Any?> =
  when (colls.size) {
    1 -> {
      val map2: Function2<Function<Any?>, Any?, LazySeq<Any?>> = ::map
      val args = apply(map2, f, colls)
      applyVar(getConcat(args), args)
    }

    2 -> {
      val map3: KFunction3<Function<Any?>, Any?, Any?, LazySeq<Any?>> = ::map
      val args = apply(map3, f, colls)
      apply(getConcat(args), args)
    }

    3 -> {
      val map4: Function4<Function<Any?>, Any?, Any?, Any?, LazySeq<Any?>> =
        ::map
      val args = apply(map4, f, colls)
      apply(getConcat(args), args)
    }

    else -> {
      val map5: KFunction5<
        Function<Any?>,
        Any?,
        Any?,
        Any?,
        Array<out Any?>,
        LazySeq<Any?>,
        > =
        ::map
      val args = applyVar(map5, f, colls)
      apply(getConcat(args), args)
    }
  }

fun mapcatVar(f: Function<Any?>, vararg colls: Any?): LazySeq<Any?> {
  val args = when (colls.size) {
    0 -> throw IllegalArgumentException("mapcatVar() colls is empty")

    1 -> {
      val mapVar: Function2<KFunction<Any?>, Any?, LazySeq<Any?>> = ::mapVar
      apply(mapVar, f, colls)
    }

    2 -> {
      val mapVar: KFunction3<KFunction<Any?>, Any?, Any?, LazySeq<Any?>> =
        ::mapVar
      apply(mapVar, f, colls)
    }

    3 -> {
      val mapVar: Function4<KFunction<Any?>, Any?, Any?, Any?, LazySeq<Any?>> =
        ::mapVar
      apply(mapVar, f, colls)
    }

    else -> {
      val map5: KFunction5<
        KFunction<Any?>,
        Any?,
        Any?,
        Any?,
        Array<out Any?>,
        LazySeq<Any?>,
        > = ::mapVar
      applyVar(map5, f, colls)
    }
  }

  return applyVar(getConcat(args), args)
}
