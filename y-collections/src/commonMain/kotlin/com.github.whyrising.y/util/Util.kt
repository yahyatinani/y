package com.github.whyrising.y.util

import com.github.whyrising.y.ArrayChunk
import com.github.whyrising.y.associative.ILookup
import com.github.whyrising.y.concretions.list.ChunkedSeq
import com.github.whyrising.y.concretions.list.PersistentList.Empty
import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.mutable.set.TransientSet
import com.github.whyrising.y.seq.*
import com.github.whyrising.y.set.PersistentSet

internal const val INIT_HASH_CODE = 0
internal const val HASH_PRIME = 31
internal const val CHUNK_SIZE = 32

enum class Category {
    INTEGER,
    FLOATING
}

fun category(n: Number): Category = when (n) {
    is Byte -> Category.INTEGER
    is Short -> Category.INTEGER
    is Int -> Category.INTEGER
    is Long -> Category.INTEGER
    is Float -> Category.FLOATING
    is Double -> Category.FLOATING
    else -> throw IllegalStateException(
        "The category of the number: $n is not supported"
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
        "The Ops of the number: $n is not supported"
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

fun <E> lazyChunkedSeq(iterator: Iterator<E>): ISeq<E> {
    if (iterator.hasNext()) {
        return LazySeq {
            val array = arrayOfNulls<Any?>(CHUNK_SIZE)

            var i = 0
            while (iterator.hasNext() && i < CHUNK_SIZE)
                array[i++] = iterator.next()

            return@LazySeq ChunkedSeq(
                ArrayChunk(array, 0, i),
                lazyChunkedSeq(iterator)
            )
        }
    }
    return Empty
}

@Suppress("UNCHECKED_CAST")
fun <E> toSeq(x: Any?): ISeq<E>? = when (x) {
    null -> null
    is ISeq<*> -> x as ISeq<E>
    is Seqable<*> -> x.seq() as ISeq<E>
    is Iterable<*> -> lazyChunkedSeq(x.iterator() as Iterator<E>)
    else -> throw IllegalArgumentException(
        "Don't know how to create ISeq from: ${x::class.simpleName}"
    )
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

@ExperimentalStdlibApi
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

@ExperimentalStdlibApi
fun hasheq(x: Any?): Int = when (x) {
    null -> 0
    is IHashEq -> x.hasheq()
    is String -> Murmur3.hashInt(x.hashCode())
    is Number -> hashNumber(x)
    else -> x.hashCode()
}

fun <E> nth(seq: Sequential, index: Int): E {
    val s = toSeq<E>(seq)

    if (index >= s!!.count || index < 0)
        throw IndexOutOfBoundsException("index = $index")

    tailrec fun get(_index: Int, e: E, rest: ISeq<E>): E {
        if (_index == index) return e

        return get(_index.inc(), rest.first(), rest.rest())
    }

    return get(0, s.first(), s.rest())
}

fun hashCombine(seed: Int, hash: Int): Int =
    seed xor hash + -0x61c88647 + (seed shl 6) + (seed shr 2)

fun <K, V> get(map: ILookup<K, V>, key: K, default: V? = null): V? =
    getFrom<K, V>(map, key, default)

fun <K, V> get(map: Map<K, V>, key: K, default: V? = null): V? =
    getFrom<K, V>(map, key, default)

fun <E> get(map: PersistentSet<E>, key: E, default: E? = null): E? =
    getFrom<E, E>(map, key, default)

fun <E> get(map: TransientSet<E>, key: E, default: E? = null): E? =
    getFrom<E, E>(map, key, default)

fun <K, V> getFrom(map: Any, key: K, default: V? = null): V? = when (map) {
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
        val message = "`${map::class.simpleName}` is not a map."
        throw IllegalArgumentException(message)
    }
}
