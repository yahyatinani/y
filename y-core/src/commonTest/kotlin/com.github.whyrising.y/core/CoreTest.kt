package com.github.whyrising.y.core

import com.github.whyrising.y.core.collections.ChunkedSeq
import com.github.whyrising.y.core.collections.IPersistentMap
import com.github.whyrising.y.core.collections.MapEntry
import com.github.whyrising.y.core.collections.PersistentArrayMap
import com.github.whyrising.y.core.collections.PersistentArrayMap.Companion.EmptyArrayMap
import com.github.whyrising.y.core.collections.PersistentHashMap
import com.github.whyrising.y.core.collections.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.core.collections.PersistentQueue
import com.github.whyrising.y.core.collections.PersistentVector
import com.github.whyrising.y.core.collections.Seqable
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4
import kotlin.reflect.KFunction5
import kotlin.reflect.KFunction6
import kotlin.reflect.KFunction7

class CoreTest : FreeSpec({
  "inc" {
    inc(1.toByte()) shouldBe 2.toByte()
    inc(1.toShort()) shouldBe 2.toShort()
    inc(1) shouldBeExactly 2
    inc(1L) shouldBeExactly 2L
    inc(1.2f) shouldBeExactly 2.2f
    inc(1.2) shouldBeExactly 2.2
  }

  "dec" {
    dec(1.toByte()) shouldBe 0.toByte()
    dec(1.toShort()) shouldBe 0.toShort()
    dec(1) shouldBeExactly 0
    dec(1L) shouldBeExactly 0L
    dec(1.2f) shouldBeExactly 1.2f.dec()
    dec(1.2) shouldBeExactly 1.2.dec()
  }

  "`identity` should return x" {
    identity(10) shouldBeExactly 10
    identity(10.1) shouldBeExactly 10.1
    identity("a") shouldBe "a"
    identity(true).shouldBeTrue()
    val f = {}
    identity(f) shouldBeSameInstanceAs f
  }

  "`str` should return the string value of the arg" {
    str() shouldBe ""
    str(null) shouldBe ""
    str(1) shouldBe "1"
    str(1, 2) shouldBe "12"
    str(1, 2, 3) shouldBe "123"
    str(1, null, 3) shouldBe "13"
    str(1, 2, null) shouldBe "12"
    str(null, 2, 3) shouldBe "23"
    str(1, 2, 3, 4) shouldBe "1234"
  }

  "curry" {
    val arg1 = 1
    val arg2 = 1.0
    val arg3 = 1.0F
    val arg4 = ""
    val arg5 = true
    val arg6 = 1L
    val f1 = { _: Int, _: Double -> 1 }
    val f2 = { _: Int, _: Double, _: Float -> 1 }
    val f3 = { _: Int, _: Double, _: Float, _: String -> 1 }
    val f4 = { _: Int, _: Double, _: Float, _: String, _: Boolean -> 1 }
    val f5 =
      { _: Int, _: Double, _: Float, _: String, _: Boolean, _: Long -> 1 }

    val curried1 = curry(f1)
    val curried2 = curry(f2)
    val curried3 = curry(f3)
    val curried4 = curry(f4)
    val curried5 = curry(f5)

    curried1(arg1)(arg2) shouldBeExactly f1(arg1, arg2)

    curried2(arg1)(arg2)(arg3) shouldBeExactly f2(arg1, arg2, arg3)

    curried3(arg1)(arg2)(arg3)(arg4) shouldBeExactly
      f3(arg1, arg2, arg3, arg4)

    curried4(arg1)(arg2)(arg3)(arg4)(arg5) shouldBeExactly
      f4(arg1, arg2, arg3, arg4, arg5)

    curried5(arg1)(arg2)(arg3)(arg4)(arg5)(arg6) shouldBeExactly
      f5(arg1, arg2, arg3, arg4, arg5, arg6)
  }

  "`complement` should return a function" {
    val f1 = { true }
    val f2 = { _: Int -> true }
    val f3 = { _: Int -> { _: Long -> true } }
    val f4 = { _: Int -> { _: Long -> { _: String -> true } } }
    val f5 = { _: Int ->
      { _: Long ->
        { _: String ->
          { _: Float ->
            true
          }
        }
      }
    }

    val complementF1 = complement(f1)
    val complementF2 = complement(f2)
    val complementF3 = complement(f3)
    val complementF4 = complement(f4)
    val complementF5 = complement(f5)

    complementF1() shouldBe false
    complementF2(0) shouldBe false
    complementF3(0)(0L) shouldBe false
    complementF4(0)(0L)("") shouldBe false
    complementF5(0)(0L)("")(1.2F) shouldBe false
  }

  "compose" - {
    "when it takes only one function f, it should return f" {
      val f: (Int) -> Int = ::identity

      compose<Int>() shouldBe ::identity
      compose(f) shouldBe f
    }

    "when g has no args, compose returns the composition with no args" {
      val f: (Int) -> String = { i: Int -> str(i) }
      val g: () -> Int = { 7 }

      val fog: () -> String = compose(f, g)

      fog() shouldBe f(g())
    }

    "when g has 1 arg, compose should return the composition with 1 arg" {
      val f: (Int) -> String = { i: Int -> str(i) }
      val g: (Float) -> Int = { 7 }

      val fog: (Float) -> String = compose(f, g)

      fog(1.2f) shouldBe f(g(1.2f))
    }

    "when g has 2 args, compose returns the composition with 2 args" {
      val x = 1.2f
      val y = 1.8
      val f: (Int) -> String = { i: Int -> str(i) }
      val g: (Float) -> (Double) -> Int = { { 7 } }

      val fog: (Float) -> (Double) -> String = compose(f, g)

      fog(x)(y) shouldBe f(g(x)(y))
    }

    "when g has 3 args, should return the composition with 3 args" {
      val x = 1.2f
      val y = 1.8
      val z = true
      val f: (Int) -> String = { i: Int -> str(i) }
      val g: (Float) -> (Double) -> (Boolean) -> Int = { { { 7 } } }

      val fog: (Float) -> (Double) -> (Boolean) -> String =
        compose(f, g)

      fog(x)(y)(z) shouldBe f(g(x)(y)(z))
    }
  }

  "ISeq component1() component2()" {
    val l = l(1, 2, 4)
    val (first, rest) = l

    first shouldBe l.first()
    rest shouldBe l.rest()
  }

  "assoc(map, key, val)" {
    assoc(null, ":a" to 15) shouldBe m(":a" to 15)
    assoc(m(":a" to 15), ":b" to 20) shouldBe m(":a" to 15, ":b" to 20)
    assoc(v(15, 56), 2 to 20) shouldBe v(15, 56, 20)
  }

  "assoc(map, key, val, kvs)" {
    val kvs: Array<Pair<String, Int>> = listOf<Pair<String, Int>>()
      .toTypedArray()

    assoc(null, ":a" to 15, *kvs) shouldBe m(":a" to 15)

    assoc(null, ":a" to 15, ":b" to 20) shouldBe m(":a" to 15, ":b" to 20)
    assoc(v(15, 56), 2 to 20, 3 to 45) shouldBe v(15, 56, 20, 45)
  }

  "assocIn(map, ks, v)" {
    assocIn(null, l(":a"), 22) shouldBe m(":a" to 22)
    assocIn(m(":a" to 11), l(":a"), 22) shouldBe m(":a" to 22)
    assocIn(v(41, 5, 6, 3), l(2), 22) shouldBe v(41, 5, 22, 3)
    assocIn(
      m(":a" to m(":b" to 45)),
      l(":a", ":b"),
      22,
    ) shouldBe m(":a" to m(":b" to 22))
    assocIn(
      v(17, 21, v(3, 5, 6)),
      l(2, 1),
      22,
    ) shouldBe v(17, 21, v(3, 22, 6))
    assocIn(
      m(":a" to m(":b" to 45)),
      l(":a", ":b"),
      m(":c" to 74),
    ) shouldBe m(":a" to m(":b" to m(":c" to 74)))
  }

  "toPmap() should return an instance of PersistentArrayMap" {
    val map = (1..8).associateWith { i -> "$i" }

    val pam: IPersistentMap<Int, String> = map.toPmap()

    (pam is PersistentArrayMap<*, *>).shouldBeTrue()
  }

  "toPmap() should return an instance of PersistentHashMap" {
    val map = (1..20).associateWith { "$it" }

    val pam: IPersistentMap<Int, String> = map.toPmap()

    (pam is PersistentHashMap<*, *>).shouldBeTrue()
  }

  "m(vararg pairs)" {
    val arrayMap = m("a" to 1)
    val pairs = (1..20).map { Pair(it, "$it") }.toTypedArray()

    m() shouldBeSameInstanceAs EmptyArrayMap

    (arrayMap is PersistentArrayMap<*, *>).shouldBeTrue()
    arrayMap.count shouldBeExactly 1
    arrayMap.containsKey("a").shouldBeTrue()

    shouldThrowExactly<IllegalArgumentException> {
      m("a" to 1, "b" to 2, "b" to 3)
    }.message shouldBe "Duplicate key: b"

    shouldThrowExactly<IllegalArgumentException> {
      m(*pairs.plus(Pair(1, "1")))
    }.message shouldBe "Duplicate key: 1"
  }

  "hashmap()" {
    val map = hashMap("a" to 1, "b" to 2, "c" to 3)
    val emptyMap = hashMap<String, Int>()

    emptyMap shouldBeSameInstanceAs PersistentHashMap.EmptyHashMap
    map.count shouldBeExactly 3
    map("a") shouldBe 1
    map("b") shouldBe 2
    map("c") shouldBe 3

    hashMap("b" to 2, "b" to 3) shouldBe hashMap("b" to 3)
  }

  "cons()" {
    cons(1, null) shouldBe l(1)
    cons(1, arrayOf<Any?>()) shouldBe l(1)
    cons(1, m()) shouldBe l(1)
    cons(1, shortArrayOf()) shouldBe l(1)
    cons(1, intArrayOf()) shouldBe l(1)
    cons(1, floatArrayOf()) shouldBe l(1)
    cons(1, doubleArrayOf()) shouldBe l(1)
    cons(1, longArrayOf()) shouldBe l(1)
    cons(1, byteArrayOf()) shouldBe l(1)
    cons(1, charArrayOf()) shouldBe l(1)
    cons(1, booleanArrayOf()) shouldBe l(1)
    cons(1, l(2, 3)) shouldBe l(1, 2, 3)
    cons(1, listOf(2, 3)) shouldBe l(1, 2, 3)
    cons(1, v(2, 3) as Seqable<*>) shouldBe l(1, 2, 3)
    cons(1, mapOf(2 to 3)) shouldBe l(1, MapEntry(2, 3))
    cons(1, intArrayOf(2, 3)) shouldBe l(1, 2, 3)
    cons(1, arrayOf('2', 3)) shouldBe l(1, '2', 3)
    cons(1, "abc") shouldBe l(1, 'a', 'b', 'c')
  }

  "v()" {
    v<Int>() shouldBeSameInstanceAs PersistentVector.EmptyVector

    v(1) shouldBe PersistentVector(1)

    v(1, 2) shouldBe PersistentVector(1, 2)

    v(1, 2, 3) shouldBe PersistentVector(1, 2, 3)

    v(1, 2, 3, 4) shouldBe PersistentVector(1, 2, 3, 4)

    v(1, 2, 3, 4, 5) shouldBe PersistentVector(1, 2, 3, 4, 5)

    v(1, 2, 3, 4, 5, 6) shouldBe PersistentVector(1, 2, 3, 4, 5, 6)

    v(1, 2, 3, 4, 5, 6, 7, 8) shouldBe
      PersistentVector(1, 2, 3, 4, 5, 6, 7, 8)
  }

  "IPersistentVector componentN()" {
    val (a, b, c, d, e, f) = v(1, 2, 3, 4, 5, 6)

    a shouldBeExactly 1
    b shouldBeExactly 2
    c shouldBeExactly 3
    d shouldBeExactly 4
    e shouldBeExactly 5
    f shouldBeExactly 6
  }

  "IPersistentVector get operator" {
    val vec = v(1, 2, 3)

    vec[0] shouldBeExactly 1
    vec[1] shouldBeExactly 2
    vec[2] shouldBeExactly 3
  }

  "IPersistentVector iterator()" {
    val vec = v(1, 2, 3)

    for ((i, n) in vec.withIndex())
      n shouldBeExactly vec[i]
  }

  "IPersistentMap iterator()" {
    val m = m(0 to 45, 1 to 55, 2 to 12)
    var i = 0
    for ((_, v) in m) {
      v as Int shouldBeExactly m[i]!! as Int
      i++
    }
  }

  "IPersistentMap get operator" {
    val m = m("a" to 1, "b" to 2, "c" to 3)

    m["a"] shouldBe 1
    m["b"] shouldBe 2
    m["c"] shouldBe 3
    m["d"].shouldBeNull()
  }

  "first()" {
    first<Int>(l(1, 2, 3)) shouldBe 1
    first<Int>(listOf(1, 2, 3)) shouldBe 1
    first<Int>(v(1, 2, 3)) shouldBe 1
    first<Int>(v<Int>()).shouldBeNull()
    first<Int>(null).shouldBeNull()
    first<Int>(arrayOf<Any?>()).shouldBeNull()
  }

  "consChunk(chunk, rest) should return rest" {
    val rest = l(1, 2)

    val r = consChunk(
      com.github.whyrising.y.core.collections.ArrayChunk(
        arrayOf(),
      ),
      rest,
    )

    r shouldBeSameInstanceAs rest
  }

  "consChunk(chunk, rest) should return ChunkedSeq" {
    val cs = consChunk(
      com.github.whyrising.y.core.collections.ArrayChunk(
        arrayOf(1, 2),
      ),
      l(3, 4),
    )

    cs.count shouldBeExactly 4
    cs.toString() shouldBe "(1 2 3 4)"
  }

  "spread()" {
    spread(null).shouldBeNull()

    spread(arrayOf(listOf(1))) shouldBe l(1)

    spread(arrayOf(1, 2, 3, listOf(4))) shouldBe l(1, 2, 3, 4)
  }

  "isEvery(pred, coll)" {
    isEvery<Int>({ true }, null).shouldBeTrue()

    isEvery<Int>({ it % 2 == 0 }, arrayOf(2, 4, 6)).shouldBeTrue()

    isEvery<Int>({ it % 2 == 0 }, arrayOf(2, 4, 1)).shouldBeFalse()

    isEvery<Int>({ it % 2 == 0 }, arrayOf(2, 4, null)).shouldBeFalse()
  }

  "conj() adds elements to a collection" {
    conj(null, 2) shouldBe l(2)

    conj(v(1), 2) shouldBe v(1, 2)

    conj(v(1), 2, 3, 4) shouldBe v(1, 2, 3, 4)

    conj(v(1), null, 3, 4) shouldBe v(1, null, 3, 4)

    conj(null, 1, 3, 4) shouldBe v(4, 3, 1)
  }

  "concat()" {
    val c = concat<Int>()

    c.count shouldBeExactly 0
    c.toString() shouldBe "()"
  }

  "concat(x)" {
    val c = concat<Int>(l(1, 2))

    c.count shouldBeExactly 2
    c.toString() shouldBe "(1 2)"
  }

  "concat(x, y)" {
    val c = concat<Int>(l(1, 2), l(3, 4))

    c.count shouldBeExactly 4
    c.toString() shouldBe "(1 2 3 4)"

    concat<Int>(null, l(3, 4)).toString() shouldBe "(3 4)"

    concat<Int>(l(1, 2), null).toString() shouldBe "(1 2)"
  }

  "concat(x, y) a ChunkedSeq" {
    val chunk1 =
      com.github.whyrising.y.core.collections.ArrayChunk(arrayOf(1, 2))

    val concatenation = concat<Int>(ChunkedSeq(chunk1), l(3, 4))

    concatenation.count shouldBeExactly 4
    concatenation.toString() shouldBe "(1 2 3 4)"
  }

  "concat(x, y, zs)" {
    val concatenation = concat<Int>(l(1, 2), l(3, 4), l(5, 6))

    concatenation.count shouldBeExactly 6
    concatenation.toString() shouldBe "(1 2 3 4 5 6)"

    concat<Int>(l(1, 2), l(3, 4), null).toString() shouldBe "(1 2 3 4)"

    concat<Int>(null, l(3, 4), l(5, 6)).toString() shouldBe "(3 4 5 6)"

    concat<Int>(l(1, 2), null, l(5, 6)).toString() shouldBe "(1 2 5 6)"

    val ch1 =
      com.github.whyrising.y.core.collections.ArrayChunk(arrayOf(1, 2))
    val ch2 =
      com.github.whyrising.y.core.collections.ArrayChunk(arrayOf(3, 4))
    val concat = concat<Int>(ChunkedSeq(ch1), ChunkedSeq(ch2), l(5, 6))
    concat.toString() shouldBe "(1 2 3 4 5 6)"

    concat<Int>(l(1, 2), listOf(3, 4), listOf(5, 6)).toString() shouldBe
      "(1 2 3 4 5 6)"

    concat<Int>(listOf(1, 2), v(3, 4), listOf(5, 6)).toString() shouldBe
      "(1 2 3 4 5 6)"
  }

  "q should return a PersistentQueue" {
    q() shouldBeSameInstanceAs PersistentQueue<Int>()
    q(null) shouldBeSameInstanceAs PersistentQueue<Int>()
    q(l(1, 2, 3, 4)) shouldBe q().conj(1).conj(2).conj(3).conj(4)
    q(v(1, 2, 3, 4)) shouldBe q().conj(1).conj(2).conj(3).conj(4)
    q(listOf(1, 2)) shouldBe q().conj(1).conj(2)
  }

  "Collections.seq()" {
    listOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    arrayOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    arrayListOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    sequenceOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    "abcd".seq() shouldBe l('a', 'b', 'c', 'd')
    shortArrayOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    intArrayOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    floatArrayOf(1f, 2f, 3f, 4f).seq() shouldBe l(1f, 2f, 3f, 4f)
    doubleArrayOf(1.1, 2.4).seq() shouldBe l(1.1, 2.4)
    longArrayOf(1, 2, 3, 4).seq() shouldBe l(1, 2, 3, 4)
    byteArrayOf(1, 2, 3, 4).seq() shouldBe l<Byte>(1, 2, 3, 4)
    charArrayOf('a', 'b', 'c', 'd').seq() shouldBe l('a', 'b', 'c', 'd')
    booleanArrayOf(true, false).seq() shouldBe l(true, false)
    mapOf(1 to 2, 3 to 4).seq() shouldBe l(MapEntry(1, 2), MapEntry(3, 4))
  }

  "m(vararg array)" - {
    "when array empty, return empty an map" {
      com.github.whyrising.y.core.util.m<String, Int>() shouldBeSameInstanceAs
        EmptyArrayMap
    }

    "when array size <= HASHTABLE_THRESHOLD, return PersistentArrayMap" {
      com.github.whyrising.y.core.util.m<String, Int>("a", 1) shouldBe
        m("a" to 1)

      com.github.whyrising.y.core.util.m<String, Int>(
        "a", 1,
        "b", 1,
        "c", 1,
        "d", 1,
        "e", 1,
        "f", 1,
        "g", 1,
        "h", 1,
      ) shouldBe m(
        "a" to 1,
        "b" to 1,
        "c" to 1,
        "d" to 1,
        "e" to 1,
        "f" to 1,
        "g" to 1,
        "h" to 1,
      )
    }

    "when array size is > HASHTABLE_THRESHOLD, return PersistentHashMap" {
      com.github.whyrising.y.core.util.m<String, Int>(
        "a", 1,
        "b", 1,
        "c", 1,
        "d", 1,
        "e", 1,
        "f", 1,
        "g", 1,
        "h", 1,
        "i", 1,
      ) shouldBe hashMap(
        "a" to 1,
        "b" to 1,
        "c" to 1,
        "d" to 1,
        "e" to 1,
        "f" to 1,
        "g" to 1,
        "h" to 1,
        "i" to 1,
      )
    }
  }

  "getIn()" {
    getIn<Any?>(mapOf("a" to 1), l("a")) shouldBe 1
    getIn<Any?>(mapOf("a" to 1), l("z")) shouldBe null
    getIn<Any?>("not-map", l("z")) shouldBe null
    getIn<Any?>(mapOf("a" to m("b" to 2)), l("a", "b")) shouldBe 2
    getIn<Any?>(mapOf("a" to m("b" to 2)), l("a", "e")) shouldBe null
    getIn(m("a" to m("b" to 2)), l("a", "e"), -1) shouldBe -1
  }

  "Associative.get(k) op" {
    m("a" to 2).assoc("b", 56)["b"] shouldBe 56
    m("a" to 2)["a"] shouldBe 2
  }

  "get(map,key)" - {
    "assertions" {
      val am = m(":a" to 5, ":b" to 6, ":c" to 3)

      get<Int>(m(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
      get<Int>(v(5, 6, 9, 3), 0) shouldBe 5
      get<Int>(hashSet(54, 69, 36), 54) shouldBe 54
      get<Int>(TransientHashSet(am.asTransient()), ":a") shouldBe 5
      get<String>(m(1 to "d"), 1) shouldBe "d"
      get<Int>(m(":a" to 15, ":b" to 74), ":a") shouldBe 15
      get<Int>(null, ":a").shouldBeNull()
      get<Int>(listOf(1, 5, 3), ":a") shouldBe null
    }

    "get(map, key) should return null" {
      val m = m(":a" to 5, ":b" to 6, ":c" to 3)

      get<Int?>(m(":a" to 1, ":b" to 2, ":c" to 3), ":x").shouldBeNull()
      get<Int?>(v(5, 6, 9, 3), 10).shouldBeNull()
      get<Int?>(hashSet(54, 69, 36), 66).shouldBeNull()
      get<Int?>(TransientHashSet(m.asTransient()), ":x")
        .shouldBeNull()
      get<Int?>(m(":a" to 15, ":b" to 74), ":x").shouldBeNull()
    }

    "get(map, key) should return default" {
      val am = m(":a" to 5, ":b" to 6, ":c" to 3)
      val transientHashSet = TransientHashSet(am.asTransient())

      get(m(":a" to 1, ":b" to 2, ":c" to 3), ":x", -1) shouldBe -1
      get(v(5, 6, 9, 3), 10, -1) shouldBe -1
      get(hashSet(54, 69, 36), 66, -1) shouldBe -1
      get(transientHashSet, ":x", -1) shouldBe -1
      get(m(":a" to 15, ":b" to 74), ":x", -1) shouldBe -1
    }
  }

  "list" {
    shouldThrowExactly<IllegalArgumentException> { prepend(1) }
    prepend(null) shouldBe null
    prepend(v(4, 5, 6)) shouldBe l(4, 5, 6)

    prepend(null, v(4, 5, 6)) shouldBe l(null, 4, 5, 6)
    prepend(1, null) shouldBe l(1)
    prepend(l<Any>(), v(4, 5, 6)) shouldBe l(l<Any>(), 4, 5, 6)
    prepend(1, l<Any>()) shouldBe l(1)
    prepend(1, v(4, 5, 6)) shouldBe l(1, 4, 5, 6)

    prepend(1, 2, v(4, 5, 6)) shouldBe l(1, 2, 4, 5, 6)

    prepend(1, 2, 3, v(4, 5, 6)) shouldBe l(1, 2, 3, 4, 5, 6)

    prepend(0, 1, 2, 3, v(4, 5, 6)) shouldBe l(0, 1, 2, 3, 4, 5, 6)
    prepend(-1, 0, 1, 2, 3, v(4, 5, 6)) shouldBe l(-1, 0, 1, 2, 3, 4, 5, 6)
  }

  "apply()" - {
    fun testFn(): String = ""

    fun g(x: Any?, y: Any?, z: Any?, z1: Any?): String = "$x$y$z$z1"
    fun g(x: Any?, y: Any?, z: Any?, z1: Any?, z2: Any?): String =
      "$x$y$z$z1$z2"

    fun g(x: Any?, y: Any?, z: Any?, z1: Any?, z2: Any?, z3: Any?): String =
      "$x$y$z$z1$z2$z3"

    fun g(
      x: Any?,
      y: Any?,
      z: Any?,
      z1: Any?,
      z2: Any?,
      z3: Any?,
      z4: Any?,
    ): String =
      "$x$y$z$z1$z2$z3$z4"

    "apply(f, args)" - {
      "when f is a lambda" - {
        "0 arg" {
          apply({ "a" }, null) shouldBe "a"

          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v<Any>())
          }.message shouldContain
            "Wrong number of args 0 passed to"

          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, null)
          }.message shouldContain
            "Wrong number of args 0 passed to"
        }

        "1 arg" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, v(1))
          }.message shouldContain
            "Wrong number of args 1 passed to"

          apply({ x: Any? -> "$x" }, v(1)) shouldBe "1"
        }

        "2 args" {
          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v(1, 2))
          }.message shouldContain
            "Wrong number of args 2 passed to"

          apply({ x: Any?, y: Any? -> "$x$y" }, v(1, 2)) shouldBe "12"
        }

        "3 args" {
          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v(1, 2, 3))
          }.message shouldContain
            "Wrong number of args 3 passed to"

          apply(
            { x: Any?, y: Any?, z: Any? -> "$x$y$z" },
            v(1, 2, 3),
          ) shouldBe "123"
        }

        "4 args" {
          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v(1, 2, 3, 4))
          }.message shouldContain
            "Wrong number of args 4 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any? -> "$a$b$c$d" },
            v(1, 2, 3, 4),
          ) shouldBe "1234"
        }

        "5 args" {
          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v(1, 2, 3, 4, 5))
          }.message shouldContain
            "Wrong number of args 5 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any? -> "$a$b$c$d$e" },
            v(1, 2, 3, 4, 5),
          ) shouldBe "12345"
        }

        "6 args" {
          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v(1, 2, 3, 4, 5, 6))
          }.message shouldContain
            "Wrong number of args 6 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any?, f: Any? ->
              "$a$b$c$d$e$f"
            },
            v(1, 2, 3, 4, 5, 6),
          ) shouldBe "123456"
        }

        "7 args" {
          shouldThrowExactly<ArityException> {
            apply({ x: Any? -> "$x" }, v(1, 2, 3, 4, 5, 6, 7))
          }.message shouldContain
            "Wrong number of args 7 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any?, f: Any?, g: Any? ->
              "$a$b$c$d$e$f$g"
            },
            v(1, 2, 3, 4, 5, 6, 7),
          ) shouldBe "1234567"
        }
      }

      "when f is a KFunction<R>" - {
        "0 or null args" {
          val str0: KFunction0<String> = ::str
          apply(str0, null) shouldBe ""
          apply(str0, v<Any?>()) shouldBe ""

          shouldThrowExactly<ArityException> {
            apply(::s, v<Any?>())
          }.message shouldContain "Wrong number of args 0 passed to"
        }

        "1 arg" {
          apply(::s, v("Symbol-1")) shouldBe Symbol("Symbol-1")

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1))
          }.message shouldContain "Wrong number of args 1 passed to"
        }

        "2 args" {
          val str2: KFunction2<Any?, Any?, String> = ::str
          apply(f = str2, args = v("str1", "str2")) shouldBe "str1str2"
          apply(f = str2, args = v("str1", null)) shouldBe "str1"
          apply(f = str2, args = v(null, "str2")) shouldBe "str2"

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1, 2))
          }.message shouldContain "Wrong number of args 2 passed to"
        }

        "3 args" {
          val str3: KFunction3<Any?, Any?, Any?, String> = ::str
          apply(str3, v("str1", "str2", "str3")) shouldBe "str1str2str3"
          apply(str3, v("str1", "str2", null)) shouldBe "str1str2"
          apply(str3, v("str1", null, "str3")) shouldBe "str1str3"
          apply(str3, v(null, "str2", "str3")) shouldBe "str2str3"

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1, 2, 3))
          }.message shouldContain "Wrong number of args 3 passed to"
        }

        "4 args" {
          val g: KFunction4<Any?, Any?, Any?, Any?, String> = ::g
          apply(g, v(1, 2, 3, 4)) shouldBe "1234"

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1, 2, 3, 4))
          }.message shouldContain "Wrong number of args 4 passed to"
        }

        "5 args" {
          val g: KFunction5<Any?, Any?, Any?, Any?, Any?, String> = ::g
          apply(g, v(1, 2, 3, 4, 5)) shouldBe "12345"

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1, 2, 3, 4, 5))
          }.message shouldContain "Wrong number of args 5 passed to"
        }

        "6 args" {
          val g: KFunction6<Any?, Any?, Any?, Any?, Any?, Any?, String> = ::g
          apply(g, v(1, 2, 3, 4, 5, 6)) shouldBe "123456"

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1, 2, 3, 4, 5, 6))
          }.message shouldContain "Wrong number of args 6 passed to"
        }

        "7 args" {
          val g: KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, String> =
            ::g
          apply(g, v(1, 2, 3, 4, 5, 6, 7)) shouldBe "1234567"

          shouldThrowExactly<ArityException> {
            val f: Function0<String> = ::testFn
            apply(f, v(1, 2, 3, 4, 5, 6, 7))
          }.message shouldContain "Wrong number of args 7 passed to"
        }

        "more" {
          val g: KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, String> =
            ::g
          shouldThrowExactly<NotImplementedError> {
            apply(g, 0, 1, 2, 3, 4, 5, 6, v(9))
          }.message shouldBe "An operation is not implemented: apply() " +
            "supports a maximum arity of 7 for now"
        }
      }
    }

    "apply(f, x, ..., args)" - {
      "when f is a lambda" - {
        "1 arg" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, v<Any>())
          }.message shouldContain
            "Wrong number of args 1 passed to"

          apply({ x: Any? -> "$x" }, 0, null) shouldBe "0"
        }

        "2 args" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, v(1))
          }.message shouldContain
            "Wrong number of args 2 passed to"

          apply({ x: Any?, y: Any? -> "$x$y" }, 0, v(1)) shouldBe "01"
          apply(
            { x: Any?, y: Any?, z: Any? -> "$x$y$z" },
            0,
            v(1, 2),
          ) shouldBe "012"
        }

        "3 args" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, 1, v(1))
          }.message shouldContain
            "Wrong number of args 3 passed to"

          apply(
            { x: Any?, y: Any?, z: Any? -> "$x$y$z" },
            0,
            1,
            v(2),
          ) shouldBe "012"
        }

        "4 args" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, 1, 2, v(1))
          }.message shouldContain
            "Wrong number of args 4 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any? -> "$a$b$c$d" },
            0,
            1,
            2,
            v(2),
          ) shouldBe "0122"
        }

        "5 args" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, 1, 2, 3, v(1))
          }.message shouldContain
            "Wrong number of args 5 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any? ->
              "$a$b$c$d$e"
            },
            0,
            1,
            2,
            3,
            v(2),
          ) shouldBe "01232"
        }

        "6 args" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, 1, 2, 3, 4, v(1))
          }.message shouldContain
            "Wrong number of args 6 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any?, f: Any? ->
              "$a$b$c$d$e$f"
            },
            0,
            1,
            2,
            3,
            4,
            v(2),
          ) shouldBe "012342"
        }

        "7 args" {
          shouldThrowExactly<ArityException> {
            apply({ "a" }, 0, 1, 2, 3, 4, 5, v(1))
          }.message shouldContain
            "Wrong number of args 7 passed to"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any?, f: Any?, g: Any? ->
              "$a$b$c$d$e$f$g"
            },
            0,
            1,
            2,
            3,
            4,
            v(2, 9),
          ) shouldBe "0123429"

          apply(
            { a: Any?, b: Any?, c: Any?, d: Any?, e: Any?, f: Any?, g: Any? ->
              "$a$b$c$d$e$f$g"
            },
            0,
            1,
            2,
            3,
            4,
            5,
            v(9),
          ) shouldBe "0123459"
        }

        "more" {
          shouldThrowExactly<NotImplementedError> {
            apply(
              {
                  a: Any?, b: Any?, c: Any?, d: Any?, e: Any?, f: Any?, g: Any?,
                  h: Any?,
                ->
                "$a$b$c$d$e$f$g$h"
              },
              0, 1, 2, 3, 4, 5, 6, v(9),
            )
          }.message shouldBe "An operation is not implemented: apply() " +
            "supports a maximum arity of 7 for now"
        }
      }
    }
  }

  "updateIn()" - {
    val users = v<Any?>(
      m("name" to "user1", "age" to 26),
      m("name" to "user2", "age" to 29),
    )
    updateIn(users, l(1, "age"), Int::inc) shouldBe v<Any?>(
      m("name" to "user1", "age" to 26),
      m("name" to "user2", "age" to 30),
    )

    updateIn(m("name" to "user1", "age" to 26), l("age"), Int::inc) shouldBe
      m("name" to "user1", "age" to 27)

    fun add(x: Int, y: Int, z: Int): Int = x + y + z
    updateIn(m(":a" to 3), l(":a"), ::add, 7, 1) shouldBe m(":a" to 11)
  }

  "map()" - {
    "map(f, coll)" {
      map(::inc, l(4, 6, 8)) shouldBe l(5, 7, 9)
      map(::inc, listOf(1, 3)) shouldBe l(2, 4)
      map({ i: Int -> i + 1 }, l(4, 6, 8)) shouldBe l(5, 7, 9)
      map({ i: Int -> i + 1 }, listOf(1, 3)) shouldBe l(2, 4)
      val str: Function1<Any?, String> = ::str
      map(str, l(4, 6, 8)) shouldBe l("4", "6", "8")

      map(
        { f: Function1<Any?, Any?> -> f(0) },
        l(Int::inc, Int::dec, ::str),
      ) shouldBe l(1, -1, "0")
    }

    "map(f, coll1, coll2)" {
      map({ i: Int, j: Int -> i + j }, l(6, 5), l(4, 2)) shouldBe l(10, 7)
      val str: Function2<Any?, Any?, String> = ::str
      map(str, l(6, 5), l(4, 2)) shouldBe l("64", "52")
      map(str, l(6, 5), null) shouldBe l()
      map(str, null, null) shouldBe l()
      map(str, null, l(4, 2)) shouldBe l()
      map({ i: Int, j: Int -> i + j }, l(3, 5), l(4)) shouldBe l(7)
      map(
        { i: Float, j: Float -> "${i + j}" },
        l(3f, 5f),
        l(4.1f, 2.3f),
      ) shouldBe l("7.1", "7.3")
    }

    "map(f, coll1, coll2, coll3)" {
      map(
        { i: Int, j: Int, k: Int -> i + j + k },
        l(3, 5),
        l(4, 2),
        l(1, 2),
      ) shouldBe l(8, 9)

      map(
        { i: Int, j: Int, k: Int -> i + j + k },
        l(3, 5),
        l(4),
        l(1, 1),
      ) shouldBe l(8)

      val str: Function3<Any?, Any?, Any?, String> = ::str
      map(str, l(3, 5), l(4.1f, 2.3f), l(true, false)) shouldBe
        l("34.1true", "52.3false")
    }

    "map(f, c1, c2, c3, vararg colls" {
      map(
        { i: Int, j: Int, k: Int, l: Int, m: Int -> i + j + k + l + m },
        l(3, 5),
        l(4, 2),
        l(1, 2),
        l(1, 1),
        l(1, 1),
      ) shouldBe l(10, 11)

      val v: Function5<Any?, Any?, Any?, Any?, Any?, Any?> = ::v
      map(
        v,
        l(3, 5),
        l(4, 2),
        l(1, 2),
        l(1, 1),
        l(1, 1),
      ) shouldBe l(v(3, 4, 1, 1, 1), v(5, 2, 2, 1, 1))
    }
  }

  "merge(vararg maps)" {
    merge(null) shouldBe null
    merge(null, null) shouldBe null
    merge(m(), m()) shouldBe m()
    merge(null, m(), m()) shouldBe m()
    merge(m(), m(":b" to 9)) shouldBe m(":b" to 9)
    merge(m(), mapOf(":b" to 9)) shouldBe m(":b" to 9)
    merge(null, m(), m(":b" to 9)) shouldBe m(":b" to 9)
    merge(m(":a" to 1, ":b" to 2, ":c" to 3), m(":b" to 5, ":d" to 9)) shouldBe
      m(":a" to 1, ":b" to 5, ":c" to 3, ":d" to 9)
  }

  "selectKeys()" {
    selectKeys(null, l(":a")) shouldBe m()

    selectKeys(m(":a" to 1, ":b" to 2), l(":a")) shouldBe m(":a" to 1)
    selectKeys(mapOf(":a" to 1, ":b" to 2), l(":a")) shouldBe m(":a" to 1)

    selectKeys(v(":a", 1, ":b", 2), l(0)) shouldBe m(0 to ":a")

    shouldThrowExactly<IllegalArgumentException> {
      selectKeys(1, l(0))
    }.message shouldBe "find not supported on type: Int"
  }

  "into(to: T, from: Any?)" {
    into(null, v<Any?>()) shouldBe null
    into(v<Any?>(), l(1, 2, 3)) shouldBe v(1, 2, 3)
    into(v(1, 2, 3), v(1, 2, 3)) shouldBe v(1, 2, 3, 1, 2, 3)
    into(null, v(3)) shouldBe l(3)

    into(
      m(":x" to 4),
      v(m(":a" to 1), m(":b" to 2), m(":c" to 3)),
    ) shouldBe m(":x" to 4, ":a" to 1, ":b" to 2, ":c" to 3)

    into(m(), v(v(":a", 1), v(":b", 2))) shouldBe m(":a" to 1, ":b" to 2)
  }

  "ISeq<T>.reduce" {
    lazySeq<Int> { v(1, 2, 3) }.reduce { acc: Int, i: Int ->
      acc + i
    } shouldBe 6

    shouldThrowExactly<UnsupportedOperationException> {
      v<Int>().seq().reduce { acc: Int, i: Int -> acc + i }
    }.message shouldBe "Empty sequence can't be reduced."
  }
})
