package com.github.whyrising.y

import com.github.whyrising.y.core.ArityException
import com.github.whyrising.y.core.collections.ISeq
import com.github.whyrising.y.core.cons
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
  while (s != null) {
    ret[i] = s.first()
    ++i
    s = s.next()
  }
  return ret
}

fun <R> applyVar(f: KFunction<R>, args: Any?): R {
  var argsSeq = seq<Any?>(args)
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

    else -> TODO("Arity ${argsSeq?.count} not supported")
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
