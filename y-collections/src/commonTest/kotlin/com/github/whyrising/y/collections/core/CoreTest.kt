package com.github.whyrising.y.collections.core

import com.github.whyrising.y.collections.ArrayChunk
import com.github.whyrising.y.collections.concretions.list.ChunkedSeq
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.map.MapEntry
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.EmptyHashMap
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.seq.Seqable
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class CoreTest {
    @Test
    fun `ISeq component1() component2()`() {
        val l = l(1, 2, 4)
        val (first, rest) = l

        first shouldBe l.first()
        rest shouldBe l.rest()
    }

    @Test
    fun `assoc(map, key, val)`() {
        assoc(null, ":a" to 15) shouldBe m(":a" to 15)
        assoc(m(":a" to 15), ":b" to 20) shouldBe m(":a" to 15, ":b" to 20)
        assoc(v(15, 56), 2 to 20) shouldBe v(15, 56, 20)
    }

    @Test
    fun `assoc(map, key, val, kvs)`() {
        val kvs: Array<Pair<String, Int>> = listOf<Pair<String, Int>>()
            .toTypedArray()

        assoc(null, ":a" to 15, *kvs) shouldBe m(":a" to 15)

        assoc(null, ":a" to 15, ":b" to 20) shouldBe m(":a" to 15, ":b" to 20)
        assoc(v(15, 56), 2 to 20, 3 to 45) shouldBe v(15, 56, 20, 45)
    }

    @Test
    fun `assocIn(map, ks, v)`() {
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

    @Test
    fun `toPmap() should return an instance of PersistentArrayMap`() {
        val map = (1..8).associateWith { i -> "$i" }

        val pam: IPersistentMap<Int, String> = map.toPmap()

        (pam is PersistentArrayMap<*, *>).shouldBeTrue()
    }

    @Test
    fun `toPmap() should return an instance of PersistentHashMap`() {
        val map = (1..20).associateWith { "$it" }

        val pam: IPersistentMap<Int, String> = map.toPmap()

        (pam is PersistentHashMap<*, *>).shouldBeTrue()
    }

    @Test
    fun `m()`() {
        val arrayMap: IPersistentMap<String, Int> = m("a" to 1)
        val pairs = (1..20).map { Pair(it, "$it") }.toTypedArray()
        val hashMap: IPersistentMap<Int, String> = m(*pairs)

        m<Int, Int>() shouldBeSameInstanceAs PersistentArrayMap.EmptyArrayMap

        (arrayMap is PersistentArrayMap<*, *>).shouldBeTrue()
        arrayMap.count shouldBeExactly 1
        arrayMap.containsKey("a").shouldBeTrue()

        (hashMap is PersistentHashMap<*, *>).shouldBeTrue()
        hashMap.count shouldBeExactly pairs.size
        hashMap shouldContainAll (1..20).map { MapEntry(it, "$it") }

        shouldThrowExactly<IllegalArgumentException> {
            m("a" to 1, "b" to 2, "b" to 3)
        }.message shouldBe "Duplicate key: b"

        shouldThrowExactly<IllegalArgumentException> {
            m(*pairs.plus(Pair(1, "1")))
        }.message shouldBe "Duplicate key: 1"
    }

    @Test
    fun `hashmap()`() {
        val map = hashMap("a" to 1, "b" to 2, "c" to 3)
        val emptyMap = hashMap<String, Int>()

        emptyMap shouldBeSameInstanceAs EmptyHashMap
        map.count shouldBeExactly 3
        map("a") shouldBe 1
        map("b") shouldBe 2
        map("c") shouldBe 3

        hashMap("b" to 2, "b" to 3) shouldBe hashMap("b" to 3)
    }

    @Test
    fun `cons()`() {
        cons(1, null) shouldBe l()
        cons(1, l(2, 3)) shouldBe l(1, 2, 3)
        cons(1, listOf(2, 3)) shouldBe l(1, 2, 3)
        cons(1, v(2, 3) as Seqable<*>) shouldBe l(1, 2, 3)
        cons(1, mapOf(2 to 3)) shouldBe l(1, MapEntry(2, 3))
        cons(1, intArrayOf(2, 3)) shouldBe l(1, 2, 3)
        cons(1, arrayOf('2', 3)) shouldBe l(1, '2', 3)
        cons(1, "abc") shouldBe l(1, 'a', 'b', 'c')
    }

    @Test
    fun `v()`() {
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

    @Test
    fun `IPersistentVector component1()`() {
        val (a) = v(1)

        a shouldBeExactly 1
    }

    @Test
    fun `IPersistentVector component2()`() {
        val (a, b) = v(1, 2)

        a shouldBeExactly 1
        b shouldBeExactly 2
    }

    @Test
    fun `IPersistentVector component3()`() {
        val (a, b, c) = v(1, 2, 3)

        a shouldBeExactly 1
        b shouldBeExactly 2
        c shouldBeExactly 3
    }

    @Test
    fun `IPersistentVector component4()`() {
        val (a, b, c, d) = v(1, 2, 3, 4)

        a shouldBeExactly 1
        b shouldBeExactly 2
        c shouldBeExactly 3
        d shouldBeExactly 4
    }

    @Test
    fun `IPersistentVector component5()`() {
        val (a, b, c, d, e) = v(1, 2, 3, 4, 5)

        a shouldBeExactly 1
        b shouldBeExactly 2
        c shouldBeExactly 3
        d shouldBeExactly 4
        e shouldBeExactly 5
    }

    @Test
    fun `IPersistentVector get operator`() {
        val vec = v(1, 2, 3)

        vec[0] shouldBeExactly 1
        vec[1] shouldBeExactly 2
        vec[2] shouldBeExactly 3
    }

    @Test
    fun `IPersistentVector iterator()`() {
        val vec = v(1, 2, 3)

        for ((i, n) in vec.withIndex())
            n shouldBeExactly vec[i]
    }

    @Test
    fun `IPersistentMap iterator()`() {
        val m = m(0 to 45, 1 to 55, 2 to 12)
        var i = 0
        for ((_, v) in m) {
            v shouldBeExactly m[i]!!
            i++
        }
    }

    @Test
    fun `IPersistentMap get operator`() {
        val m = m("a" to 1, "b" to 2, "c" to 3)

        m["a"] shouldBe 1
        m["b"] shouldBe 2
        m["c"] shouldBe 3
        m["d"].shouldBeNull()
    }

    @Test
    fun `first()`() {
        first<Int>(l(1, 2, 3)) shouldBe 1
        first<Int>(listOf(1, 2, 3)) shouldBe 1
        first<Int>(v(1, 2, 3)) shouldBe 1
        first<Int>(v<Int>()).shouldBeNull()
        first<Int>(null).shouldBeNull()
    }

    @Test
    fun `consChunk(chunk, rest) should return rest`() {
        val rest = l(1, 2)

        val r = consChunk(ArrayChunk(arrayOf()), rest)

        r shouldBeSameInstanceAs rest
    }

    @Test
    fun `consChunk(chunk, rest) should return ChunkedSeq`() {
        val cs = consChunk(ArrayChunk(arrayOf(1, 2)), l(3, 4))

        cs.count shouldBeExactly 4
        cs.toString() shouldBe "(1 2 3 4)"
    }

    @Test
    fun `concat()`() {
        val c = concat<Int>()

        c.count shouldBeExactly 0
        c.toString() shouldBe "()"
    }

    @Test
    fun `concat(x)`() {
        val c = concat<Int>(l(1, 2))

        c.count shouldBeExactly 2
        c.toString() shouldBe "(1 2)"
    }

    @Test
    fun `concat(x, y)`() {
        val c = concat<Int>(l(1, 2), l(3, 4))

        c.count shouldBeExactly 4
        c.toString() shouldBe "(1 2 3 4)"

        concat<Int>(null, l(3, 4)).toString() shouldBe "(3 4)"

        concat<Int>(l(1, 2), null).toString() shouldBe "(1 2)"
    }

    @Test
    fun `concat(x, y) ChunkedSeq`() {
        val chunk1 = ArrayChunk(arrayOf(1, 2))

        val concatenation = concat<Int>(ChunkedSeq(chunk1), l(3, 4))

        concatenation.count shouldBeExactly 4
        concatenation.toString() shouldBe "(1 2 3 4)"
    }

    @Test
    fun `concat(x, y, zs)`() {
        val concatenation = concat<Int>(l(1, 2), l(3, 4), l(5, 6))

        concatenation.count shouldBeExactly 6
        concatenation.toString() shouldBe "(1 2 3 4 5 6)"

        concat<Int>(l(1, 2), l(3, 4), null).toString() shouldBe "(1 2 3 4)"

        concat<Int>(null, l(3, 4), l(5, 6)).toString() shouldBe "(3 4 5 6)"

        concat<Int>(l(1, 2), null, l(5, 6)).toString() shouldBe "(1 2 5 6)"

        val ch1 = ArrayChunk(arrayOf(1, 2))
        val ch2 = ArrayChunk(arrayOf(3, 4))
        val concat = concat<Int>(ChunkedSeq(ch1), ChunkedSeq(ch2), l(5, 6))
        concat.toString() shouldBe "(1 2 3 4 5 6)"

        concat<Int>(l(1, 2), listOf(3, 4), listOf(5, 6)).toString() shouldBe
            "(1 2 3 4 5 6)"

        concat<Int>(listOf(1, 2), v(3, 4), listOf(5, 6)).toString() shouldBe
            "(1 2 3 4 5 6)"
    }

    @Test
    fun `map(f, coll)`() {
        val f: (Int) -> Int = { i -> i + 1 }

        map(f, null) shouldBe PersistentList.Empty

        map(f, listOf(11, 5, 9)) shouldBe l(12, 6, 10)

        val chunkedSeq = ChunkedSeq(ArrayChunk(arrayOf(11, 5, 9)))
        map(f, chunkedSeq) shouldBe listOf(12, 6, 10)

        map(f, null).toString() shouldBe "()"
    }

    @Test
    fun `map(f, c1, c2)`() {
        val f: (Int, Int) -> Int = { i, j -> i + j }

        map(f, l(5), l(8)) shouldBe l(13)

        map(f, null, null) shouldBe l()

        map(f, l(5), null) shouldBe l()

        map(f, null, l(8)) shouldBe l()
    }

    @Test
    fun `map(f, c1, c2, c3)`() {
        val f: (Int, Int, Int) -> Int = { i, j, k -> i + j + k }

        map(f, l(5), l(8), l(1)) shouldBe l(14)

        map(f, null, null, null) shouldBe l()

        map(f, l(5), null, null) shouldBe l()

        map(f, null, l(8), null) shouldBe l()
    }

    @Test
    fun `apply(f(X), x)`() {
        fun f(i: Int, vararg j: Int): Int {
            return j.fold(i) { acc, n ->
                n * acc
            }
        }

        apply<Int, Int>({ i: Int -> i * 2 }, l(5)) shouldBeExactly 10

        apply<Int, Int>({ i: Int, j: Int -> i * j }, l(5, 6)) shouldBeExactly 30

        apply({ i: Int, j: Int -> i * j }, 5, l(6)) shouldBeExactly 30

        apply({ i: Int, j: Int -> i * j }, 5, 6, l<Int>()) shouldBeExactly 30

        shouldThrowExactly<RuntimeException> {
            apply<Int, Int>({ i: Int -> i * 2 }, l<Int>())
        }
    }

    @Test
    fun `map(f, c1, c2, c3, colls)`() {
        // TODO: apply needed
    }
}
