package com.github.whyrising.y.core

import com.github.whyrising.y.concretions.map.m
import com.github.whyrising.y.concretions.vector.v
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CoreTest {
    @Test
    fun `assoc(map, key, val)`() {
        assoc(null, ":a" to 15) shouldBe m(":a" to 15)
        assoc(m(":a" to 15), ":b" to 20) shouldBe m(":a" to 15, ":b" to 20)
        assoc(v(15, 56), 2 to 20) shouldBe v(15, 56, 20)

        shouldThrowExactly<IllegalArgumentException> {
            assoc(listOf(15, 56), 2 to 20)
        }.message shouldBe "[15, 56] is not Associative"
    }

    @Test
    fun `assoc(map, key, val, kvs)`() {
        val kvs: Array<Pair<String, Int>> = listOf<Pair<String, Int>>()
            .toTypedArray()

        assoc(null, ":a" to 15, *kvs) shouldBe m(":a" to 15)

        assoc(null, ":a" to 15, ":b" to 20) shouldBe m(":a" to 15, ":b" to 20)
        assoc(v(15, 56), 2 to 20, 3 to 45) shouldBe v(15, 56, 20, 45)
    }
}
