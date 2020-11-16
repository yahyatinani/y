package com.github.whyrising.y.hashset

import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.PersistentHashSet.EmptyHashSet
import com.github.whyrising.y.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.atomicfu.atomic

class PersistentHashSetTest : FreeSpec({
    "EmptyHashSet" - {
        "map property should be set to EmptyLeanMap" {
            EmptyHashSet.map shouldBeSameInstanceAs EmptyLeanMap
        }
    }

    "TransientHashSet" - {
        "count should return the count of the inner transient map" {
            val tmap1 = atomic(m<Int, Int>().asTransient())
            val tmap2 = atomic(m("a" to "1").asTransient())

            val tSet1 = TransientHashSet(tmap1)
            val tSet2: TransientHashSet<String> = TransientHashSet(tmap2)

            tSet1.count shouldBeExactly 0
            tSet2.count shouldBeExactly tmap2.value.count
        }

        "contains(key)" {
            val map = m("a" to "1", "b" to "2", "c" to "3", null to null)
            val tSet = TransientHashSet(atomic(map.asTransient()))

            tSet.contains("a").shouldBeTrue()
            tSet.contains("b").shouldBeTrue()
            tSet.contains("c").shouldBeTrue()
            tSet.contains(null).shouldBeTrue()
            tSet.contains("x").shouldBeFalse()
        }

        "disjoin(key) should return a transient set without the key" {
            val map = m("a" to "1", "b" to "2", "c" to "3")
            val tSet = TransientHashSet(atomic(map.asTransient()))

            val newTranSet1 = tSet.disjoin("a")

            newTranSet1 shouldBeSameInstanceAs tSet
            newTranSet1.tmap.value shouldBeSameInstanceAs tSet.tmap.value
            newTranSet1.count shouldBeExactly 2
            newTranSet1.contains("a").shouldBeFalse()
            newTranSet1.contains("b").shouldBeTrue()
            newTranSet1.contains("c").shouldBeTrue()
        }

        "get(key)" {
            val map = m("a" to "1", "b" to "2", "c" to "3", null to null)
            val tSet = TransientHashSet(atomic(map.asTransient()))

            tSet["a"] shouldBe "1"
            tSet["b"] shouldBe "2"
            tSet["c"] shouldBe "3"
            tSet[null] shouldBe null
        }
    }
})
