package com.github.whyrising.y.core.collections.map

import com.github.whyrising.y.core.collections.PersistentArrayMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class PersistentArrayMapTest : FreeSpec({
    "createWithCheck(pairs) should return a PersistentArrayMap" {
        val arrayOfPairs = arrayOf("a" to 1, "b" to 2, "c" to 3)

        val map: PersistentArrayMap<String, Int> =
            PersistentArrayMap.createWithCheck(*arrayOfPairs)

        map.array shouldBe arrayOf("a", 1, "b", 2, "c", 3)
    }

    "createWithCheck(pairs) should throw exception when duplicate keys" {
        shouldThrowExactly<IllegalArgumentException> {
            PersistentArrayMap.createWithCheck("a" to 1, "b" to 2, "b" to 3)
        }.message shouldBe "Duplicate key: b"

        shouldThrowExactly<IllegalArgumentException> {
            PersistentArrayMap.createWithCheck("a" to 1, "a" to 2, "b" to 3)
        }.message shouldBe "Duplicate key: a"

        shouldThrowExactly<IllegalArgumentException> {
            PersistentArrayMap.createWithCheck("a" to 1, "b" to 2, "a" to 3)
        }.message shouldBe "Duplicate key: a"

        shouldThrowExactly<IllegalArgumentException> {
            PersistentArrayMap.createWithCheck(1L to "a", 1 to "b")
        }.message shouldBe "Duplicate key: 1"
    }
})
