package io.github.yahyatinani.y.core.util

import io.github.yahyatinani.y.core.collections.ArrayChunk
import io.github.yahyatinani.y.core.collections.ChunkedSeq
import io.github.yahyatinani.y.core.collections.HASHTABLE_THRESHOLD
import io.github.yahyatinani.y.core.collections.IHashEq
import io.github.yahyatinani.y.core.collections.IPersistentCollection
import io.github.yahyatinani.y.core.collections.IPersistentMap
import io.github.yahyatinani.y.core.collections.ISeq
import io.github.yahyatinani.y.core.collections.InstaCount
import io.github.yahyatinani.y.core.collections.LazySeq
import io.github.yahyatinani.y.core.collections.PersistentArrayMap
import io.github.yahyatinani.y.core.collections.PersistentHashMap
import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.collections.Sequential
import io.github.yahyatinani.y.core.seq

internal const val HASH_PRIME = 31
internal const val CHUNK_SIZE = 32

enum class Category {
  INTEGER,
  FLOATING,
}

fun category(n: Number): Category = when (n) {
  is Byte -> Category.INTEGER
  is Short -> Category.INTEGER
  is Int -> Category.INTEGER
  is Long -> Category.INTEGER
  is Float -> Category.FLOATING
  is Double -> Category.FLOATING
  else -> throw IllegalStateException(
    "The category of the number: $n is not supported",
  )
}

fun ops(n: Number): Ops = when (n) {
  is Byte -> LongOps
  is Short -> LongOps
  is Int -> LongOps
  is Long -> LongOps
  is Float -> DoubleOps
  is Double -> DoubleOps
  else -> throw IllegalStateException(
    "The Ops of the number: $n is not supported",
  )
}

private fun sameCategory(x: Number, y: Number) = category(x) == category(y)

private fun areEquiv(x: Number, y: Number): Boolean =
  ops(x).combine(ops(y)).equiv(x, y)

fun <E> equiv(e1: E, e2: Any?): Boolean = when {
  e1 == e2 -> true
  e1 == null || e2 == null -> false
  e1 is Number && e2 is Number -> sameCategory(e1, e2) && areEquiv(e1, e2)
  else -> when {
    e1 is IPersistentCollection<*> -> e1.equiv(e2)
    e2 is IPersistentCollection<*> -> e2.equiv(e1)
    else -> false
  }
}

fun equals(o1: Any?, o2: Any?): Boolean = when {
  o1 === o2 -> true
  else -> o1 != null && o1 == o2
}

fun <E> lazyChunkedSeq(iterator: Iterator<E>): ISeq<E> = when {
  iterator.hasNext() -> LazySeq {
    val array = arrayOfNulls<Any?>(CHUNK_SIZE)
    var i = 0
    while (iterator.hasNext() && i < CHUNK_SIZE)
      array[i++] = iterator.next()
    ChunkedSeq(
      ArrayChunk(array as Array<*>, 0, i),
      lazyChunkedSeq(iterator),
    )
  }
  else -> Empty
}

fun compareNumbers(x: Number, y: Number): Int {
  val ops = ops(x).combine(ops(y))

  return when {
    ops.lessThan(x, y) -> -1
    ops.lessThan(y, x) -> 1
    else -> 0
  }
}

@Suppress("UNCHECKED_CAST")
fun <E> compare(e1: E, e2: E): Int = when {
  e1 == e2 -> 0
  e1 != null -> when {
    e2 == null -> 1
    e1 is Number && e2 is Number -> compareNumbers(e1, e2)
    else -> (e1 as Comparable<E>).compareTo(e2)
  }
  else -> -1
}

private fun hashNumber(x: Number): Int = when {
  // TODO: BigInteger
  x is Long || x is Int || x is Short || x is Byte -> {
    val lpart: Long = x.toLong()
    Murmur3.hashLong(lpart)
  }
  x is Double -> when (x) {
    -0.0 -> 0 // match 0.0
    else -> x.hashCode()
  }
  x is Float && x == -0.0f -> 0 // match 0.0f
  // TODO: BigDecimal
  else -> x.hashCode()
}

fun hasheq(x: Any?): Int = when (x) {
  null -> 0
  is IHashEq -> x.hasheq()
  is String -> Murmur3.hashInt(x.hashCode())
  is Number -> hashNumber(x)
  else -> x.hashCode()
}

fun <E> nth(seq: Sequential, index: Int): E {
  val s = seq(seq)

  if (index >= s!!.count || index < 0) {
    throw IndexOutOfBoundsException("index = $index")
  }

  tailrec fun get(_index: Int, e: E, rest: ISeq<E>): E {
    if (_index == index) return e

    return get(_index.inc(), rest.first(), rest.rest())
  }

  return get(0, s.first() as E, s.rest() as ISeq<E>)
}

fun hashCombine(seed: Int, hash: Int): Int =
  seed xor hash + -0x61c88647 + (seed shl 6) + (seed shr 2)

fun count(a: Any?): Int = when (a) {
  is InstaCount -> a.count
  else -> TODO("Not yet implemented")
}

fun <K, V> m(vararg a: Any?): IPersistentMap<K, V> = when {
  a.isEmpty() -> PersistentArrayMap.EmptyArrayMap
  a.size <= HASHTABLE_THRESHOLD -> PersistentArrayMap.createWithCheck(*a)
  else -> PersistentHashMap.createWithCheck(a as Array<Any?>)
}
