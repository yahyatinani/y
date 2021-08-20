package com.github.whyrising.y.collections.map

import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.Companion.createWithCheck
import com.github.whyrising.y.collections.core.m
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PersistentHashMapTest {
    @Test
    fun `createWithCheck(pairs) should throw exception when duplicate keys`() {
        shouldThrowExactly<IllegalArgumentException> {
            createWithCheck("a" to 1, "b" to 2, "c" to 3, "c" to 4)
        }.message shouldBe "Duplicate key: c"
    }

    @Test
    fun `create(pairs) should last duplicate key value`() {
        val m = PersistentHashMap.create("a" to 1, "b" to 2, "c" to 3, "c" to 4)

        m shouldBe m("a" to 1, "b" to 2, "c" to 4)
    }
}
