package com.github.whyrising.y.hashset

import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.PersistentHashSet
import com.github.whyrising.y.PersistentHashSet.EmptyHashSet
import com.github.whyrising.y.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.TransientSet
import com.github.whyrising.y.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
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

            val newTranSet1 = tSet.disjoin("a") as TransientHashSet<String>

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

        "conj(e)" {
            val e = "7"
            val gen = Arb.set(Arb.string().filter { it != e })
            checkAll(gen) { set: Set<String> ->
                val l = set.map { s: String -> Pair(s, s) }
                val map = m(*l.toTypedArray())
                val tSet = TransientHashSet(atomic(map.asTransient()))

                val newTranSet: TransientSet<String> = tSet.conj(e)
                val transientHashSet = newTranSet as TransientHashSet<String>

                newTranSet.count shouldBeExactly map.count + 1
                newTranSet.contains(e)
                transientHashSet.tmap.value.valAt(e) shouldBe e
            }
        }

        @Suppress("UNCHECKED_CAST")
        "persistent() should return a PersistentHashSet" {
            val map = m("a" to "1", "b" to "2", "c" to "3")
            val tSet = TransientHashSet(atomic(map.asTransient()))

            val set = tSet.persistent() as PersistentHashSet<String>

            set.map.count shouldBeExactly map.count
            set.map shouldBe map
        }

        "invoke(key, default)" {
            val map = m("a" to "1", "b" to "2", "c" to "3")
            val tSet = TransientHashSet(atomic(map.asTransient()))
            val default = "notFound"

            tSet("a", default) shouldBe "1"
            tSet("b", default) shouldBe "2"
            tSet("x", default) shouldBe default
        }

        "invoke(key)" {
            val map = m("a" to "1", "b" to "2", "c" to "3")
            val tSet = TransientHashSet(atomic(map.asTransient()))

            tSet("a") shouldBe "1"
            tSet("b") shouldBe "2"
            tSet("x") shouldBe null
        }
    }
})
