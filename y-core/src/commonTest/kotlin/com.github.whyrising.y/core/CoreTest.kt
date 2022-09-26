package com.github.whyrising.y.core

import com.github.whyrising.y.core.collections.ChunkedSeq
import com.github.whyrising.y.core.collections.IPersistentMap
import com.github.whyrising.y.core.collections.MapEntry
import com.github.whyrising.y.core.collections.PersistentArrayMap
import com.github.whyrising.y.core.collections.PersistentArrayMap.Companion.EmptyArrayMap
import com.github.whyrising.y.core.collections.PersistentHashMap
import com.github.whyrising.y.core.collections.PersistentList.Empty
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
import io.kotest.matchers.types.shouldBeSameInstanceAs

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
      22
    ) shouldBe m(":a" to m(":b" to 22))
    assocIn(
      v(17, 21, v(3, 5, 6)),
      l(2, 1),
      22
    ) shouldBe v(17, 21, v(3, 22, 6))
    assocIn(
      m(":a" to m(":b" to 45)),
      l(":a", ":b"),
      m(":c" to 74)
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
    val arrayMap: IPersistentMap<String, Int> = m("a" to 1)
    val pairs = (1..20).map { Pair(it, "$it") }.toTypedArray()

    m<Int, Int>() shouldBeSameInstanceAs EmptyArrayMap

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
      v shouldBeExactly m[i]!!
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
  }

  "consChunk(chunk, rest) should return rest" {
    val rest = l(1, 2)

    val r = consChunk(
      com.github.whyrising.y.core.collections.ArrayChunk(
        arrayOf()
      ),
      rest
    )

    r shouldBeSameInstanceAs rest
  }

  "consChunk(chunk, rest) should return ChunkedSeq" {
    val cs = consChunk(
      com.github.whyrising.y.core.collections.ArrayChunk(
        arrayOf(1, 2)
      ),
      l(3, 4)
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
    q<Int>() shouldBeSameInstanceAs PersistentQueue<Int>()
    q<Int>(null) shouldBeSameInstanceAs PersistentQueue<Int>()
    q<Int>(l(1, 2, 3, 4)) shouldBe q<Int>().conj(1).conj(2).conj(3).conj(4)
    q<Int>(v(1, 2, 3, 4)) shouldBe q<Int>().conj(1).conj(2).conj(3).conj(4)
    q<Int>(listOf(1, 2)) shouldBe q<Int>().conj(1).conj(2)
  }

  "map()" - {
    "mapping f to one collection" {
      map<Int, String>(l<Int>()) { "${it * 2}" } shouldBe Empty
      map<Int, String>(l(1, 3, 2)) { "${it * 2}" } shouldBe
        l("2", "6", "4")
      map<Int, String>(listOf(1, 3)) { "${it * 3}" } shouldBe l("3", "9")
      var i = 0
      val lazySeq = map<Int, String>(listOf(1, 3, 4, 2)) {
        i++ // to prove laziness, f is applied as the element is needed
        "${it * 2}"
      }
      lazySeq.first() shouldBe "2"
      i shouldBeExactly 1
    }

    "mapping f to two collections" {
      map<Int, Int, Int>(l(3, 5), l(4, 2)) { i, j ->
        i + j
      } shouldBe l(7, 7)

      map<Int, Int, Int>(l(3, 5), l(4)) { i, j ->
        i + j
      } shouldBe l(7)

      map<Int, Float, String>(l(3, 5), l(4.1f, 2.3f)) { i, j ->
        "${i + j}"
      } shouldBe l("7.1", "7.3")
    }

    "mapping f to three collections" {
      map<Int, Int, Int, Int>(l(3, 5), l(4, 2), l(1, 1)) { i, j, k ->
        i + j + k
      } shouldBe l(8, 8)

      map<Int, Int, Int, Int>(l(3, 5), l(4), l(1, 1)) { i, j, k ->
        i + j + k
      } shouldBe l(8)

      map<Int, Float, Boolean, String>(
        l(3, 5),
        l(4.1f, 2.3f),
        l(true, false)
      ) { i, j, k ->
        "${i + j}$k"
      } shouldBe l("7.1true", "7.3false")
    }
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
        "h", 1
      ) shouldBe m(
        "a" to 1,
        "b" to 1,
        "c" to 1,
        "d" to 1,
        "e" to 1,
        "f" to 1,
        "g" to 1,
        "h" to 1
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
        "i", 1
      ) shouldBe hashMap(
        "a" to 1,
        "b" to 1,
        "c" to 1,
        "d" to 1,
        "e" to 1,
        "f" to 1,
        "g" to 1,
        "h" to 1,
        "i" to 1
      )
    }
  }

  "getIn()" {
    getIn(mapOf("a" to 1), l("a")) shouldBe 1
    getIn(mapOf("a" to 1), l("z")) shouldBe null
    getIn(mapOf("a" to m("b" to 2)), l("a", "b")) shouldBe 2
    getIn(mapOf("a" to m("b" to 2)), l("a", "e")) shouldBe null
    getIn(m("a" to m("b" to 2)), l("a", "e"), -1) shouldBe -1
  }

  "Associative.get(k) op" {
    m("a" to 2).assoc("b", 56)["b"] shouldBe 56
    (m("a" to 2) as IPersistentMap<*, *>)["a"] shouldBe 2
  }
})
