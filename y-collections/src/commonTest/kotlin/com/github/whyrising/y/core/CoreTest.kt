package com.github.whyrising.y.core

import com.github.whyrising.y.concretions.list.l
import com.github.whyrising.y.concretions.map.m
import com.github.whyrising.y.concretions.vector.v
import io.kotest.matchers.shouldBe
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
}
