package com.github.whyrising.y.collections.core

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
    fun `component1()`() {
        val (a) = v(1)

        a shouldBeExactly 1
    }

    @Test
    fun `component2()`() {
        val (a, b) = v(1, 2)

        a shouldBeExactly 1
        b shouldBeExactly 2
    }

    @Test
    fun `component3()`() {
        val (a, b, c) = v(1, 2, 3)

        a shouldBeExactly 1
        b shouldBeExactly 2
        c shouldBeExactly 3
    }

    @Test
    fun `component4()`() {
        val (a, b, c, d) = v(1, 2, 3, 4)

        a shouldBeExactly 1
        b shouldBeExactly 2
        c shouldBeExactly 3
        d shouldBeExactly 4
    }

    @Test
    fun `component5()`() {
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
}
