package com.github.whyrising.y.hashset

import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.LeanMap.NodeIterator.NodeIter
import com.github.whyrising.y.MapIterable
import com.github.whyrising.y.Murmur3
import com.github.whyrising.y.PersistentHashSet
import com.github.whyrising.y.PersistentHashSet.EmptyHashSet
import com.github.whyrising.y.PersistentHashSet.HashSet
import com.github.whyrising.y.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.PersistentSet
import com.github.whyrising.y.TransientSet
import com.github.whyrising.y.hashMap
import com.github.whyrising.y.hashSet
import com.github.whyrising.y.hs
import com.github.whyrising.y.l
import com.github.whyrising.y.m
import com.github.whyrising.y.mocks.MockPersistentMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.atomicfu.atomic

@ExperimentalStdlibApi
class PersistentHashSetTest : FreeSpec({
    "empty() should return EmptyHashSet" {
        val map = hashMap("a" to "1", "b" to "2", "c" to "3")

        EmptyHashSet.empty() shouldBeSameInstanceAs EmptyHashSet
        HashSet(map).empty() shouldBeSameInstanceAs EmptyHashSet
    }

    "contains()" {
        val map = hashMap("a" to "1", "b" to "2", "c" to "3")
        val emptySet: PersistentHashSet<String> = EmptyHashSet
        val set: PersistentHashSet<String> = HashSet(map)

        emptySet.contains("a").shouldBeFalse()
        set.contains("x").shouldBeFalse()

        set.contains("a").shouldBeTrue()
        set.contains("b").shouldBeTrue()
        set.contains("c").shouldBeTrue()
    }

    "disjoin(key)" {
        val element = "a"
        val set = hs(element, "b", "c")

        val newSet1: PersistentSet<String> = set.disjoin(element)
        val newSet2: PersistentSet<String> = set.disjoin("x")
        val newSet3: PersistentSet<String> = set.disjoin(element)
            .disjoin("b").disjoin("c")

        newSet1.count shouldBeExactly set.count - 1
        newSet1.contains(element).shouldBeFalse()

        newSet2 shouldBeSameInstanceAs set
        newSet3 shouldBeSameInstanceAs EmptyHashSet
    }

    "get(key)" {
        val set = hs("a", "b", "c")

        set["a"] shouldBe "a"
        set["b"] shouldBe "b"
        set["c"] shouldBe "c"
    }

    "conj(e)" - {
        "when e already exits, it should return this" {
            val map = hashMap("a" to "1", "b" to "2", "c" to "3")
            val set = HashSet(map)

            val newSet = set.conj("a")

            newSet.count shouldBeExactly map.count
            newSet shouldBeSameInstanceAs set
        }

        "when e is new, it should return add it in a new set" {
            val map = hashMap("a" to "1", "b" to "2", "c" to "3")
            val set = HashSet(map)

            val newSet: PersistentSet<String> = set.conj("x")

            newSet.count shouldBeExactly map.count + 1
            newSet shouldNotBeSameInstanceAs set
            (newSet as PersistentHashSet<String>).map.containsKey("x")
        }

    }

    "seq()" {
        val map = hashMap("a" to "1", "b" to "2", "c" to "3")
        val set = HashSet(map)

        val seq = set.seq()

        seq.count shouldBeExactly 3
        seq.first() shouldBe "a"
        seq.rest().first() shouldBe "b"
        seq.rest().rest().first() shouldBe "c"
    }

    "equiv(other)" - {
        "when other is not a Set, it should return false" {
            val map = hashMap("a" to "1", "b" to "2", "c" to "3")
            val set = HashSet(map)

            set.equiv("a").shouldBeFalse()
        }

        "when other is a Set<*>" - {
            "when sizes are different, it should return false" {
                val map = hashMap("a" to "1", "b" to "2", "c" to "3")
                val set = HashSet(map)

                set.equiv(setOf("a", "b")).shouldBeFalse()
            }

            "when sizes are equal" {
                val map = hashMap("a" to "1", "b" to "2", 1L to "3")
                val set = HashSet(map)

                set.equiv(setOf("a", "b", 2)).shouldBeFalse()
                set.equiv(setOf("a", "b", 1)).shouldBeTrue()
                set.equiv(set).shouldBeTrue()
                set.equiv(HashSet(map)).shouldBeTrue()
            }
        }
    }

    "asTransient()" {
        val map = hashMap("a" to "1", "b" to "2", 1L to "3")
        val set = HashSet(map)

        val tr = set.asTransient() as TransientHashSet<Any>
        val trMap = tr.tmap.value

        trMap.count shouldBeExactly map.count
        trMap.valAt("a") shouldBe map("a")
    }

    "createWithCheck(...elements)" {
        val array = arrayOf(1, 2, 3, 4, 5)
        val set = PersistentHashSet.createWithCheck(*array)

        set.count shouldBeExactly array.size
        set.shouldContainAll(*array)

        shouldThrowExactly<IllegalArgumentException> {
            PersistentHashSet.createWithCheck(1, 1, 2, 3, 4)
        }.message shouldBe "Duplicate key: 1"
    }

    "create(...elements)" {
        val array1 = arrayOf(1, 2, 3, 4, 5)
        val array2 = arrayOf(1, 1, 2, 3, 3, 4, 5)
        val set1 = PersistentHashSet.create(*array1)
        val set2 = PersistentHashSet.create(*array2)

        set1.count shouldBeExactly array1.size
        set1.shouldContainAll(*array1)

        set2.count shouldBeExactly array2.size - 2
        set2.shouldContainAll(*array2)
    }

    "hashSet(...elements) should call create(...elements)" {
        val array1 = arrayOf(1, 2, 3, 4, 5)
        val array2 = arrayOf(1, 1, 2, 3, 3, 4, 5)
        val set1 = hashSet(*array1)
        val set2 = hashSet(*array2)

        set1.count shouldBeExactly array1.size
        set1.shouldContainAll(*array1)

        set2.count shouldBeExactly array2.size - 2
        set2.shouldContainAll(*array2)

        hashSet<Int>() shouldBeSameInstanceAs EmptyHashSet
    }

    "hashSet(seq)" {
        val seq = l(1, 1, 2, 3, 3, 4, 5)
        val set = hashSet(seq)

        set.count shouldBeExactly seq.size - 2
        set.shouldContainAll(seq)

        hashSet<Int>() shouldBeSameInstanceAs EmptyHashSet
    }

    "hs(...elements)" {
        val array = arrayOf(1, 2, 3, 4, 5)
        val set = hs(*array)

        set.count shouldBeExactly array.size
        set.shouldContainAll(*array)

        shouldThrowExactly<IllegalArgumentException> {
            PersistentHashSet.createWithCheck(1, 1, 2, 3, 4)
        }.message shouldBe "Duplicate key: 1"

        hs<String>() shouldBeSameInstanceAs EmptyHashSet
    }

    "toString()" {
        val set = hs("a", "b", "c")

        hs<String>().toString() shouldBe "#{}"
        set.toString() shouldBe "#{a b c}"
    }

    "hashcode()" {
        val set = hs("a", "b", "c")
        val expectedHash = set.fold(0) { acc: Int, s: String ->
            acc + s.hashCode()
        }

        val hash = set.hashCode()

        hash shouldBeExactly expectedHash
        set.hashCode() shouldBeExactly expectedHash
        hs<String>().hashCode() shouldBeExactly 0
    }

    "equals(other)" - {
        "when same instance, it should return true" {
            val set = hs("a", "b", "c")

            (set == set).shouldBeTrue()
        }

        "when other is not a Set, it should return false" {
            val set = hs("a", "b", "c")

            set.equals("a").shouldBeFalse()
        }

        "when other is a Set<*>" - {
            "when sizes are different, it should return false" {
                val set = hs("a", "b", "c")

                (set == setOf("a", "b")).shouldBeFalse()
            }

            "when sizes are equal, it should check if this contains every e" {
                val set = hs("a", "b", "c")

                (set == setOf("a", "b", 2)).shouldBeFalse()
                (set == hs("a", "b", 1)).shouldBeFalse()
                (set == setOf("a", "b", "c")).shouldBeTrue()

                (EmptyHashSet == EmptyHashSet).shouldBeTrue()
                (EmptyHashSet == setOf<String>()).shouldBeTrue()
                (EmptyHashSet == hs("a").disjoin("a")).shouldBeTrue()
                (EmptyHashSet == set).shouldBeFalse()
            }
        }
    }

    "hasheq()" {
        val set = hs("a", "b", "c")
        val expected = Murmur3.hashUnordered(set)

        set.hasheq() shouldBeExactly expected
        set.hasheq() shouldBeExactly expected
    }

    "invoke(key)" {
        val set = hs("a", "b", "c")

        set("a") shouldBe "a"
        set("b") shouldBe "b"
        set("c") shouldBe "c"
        set("x") shouldBe null
    }

    "Set implementation" - {
        "size()" {
            val map = hashMap("a" to "1", "b" to "2", "c" to "3")
            val set = HashSet(map)

            set.size shouldBeExactly map.count
        }

        "iterator()" - {
            @Suppress("UNCHECKED_CAST")
            "when inner map is MapIterable, it should return NodeIter" {
                val map = hashMap("a" to "1", "b" to "2", "c" to "3")
                val mapIterable = map as MapIterable<String, String>
                val keyIterator = mapIterable.keyIterator()
                val set = HashSet(map)

                val iter = set.iterator() as NodeIter<String, String, String>

                iter.next() shouldBe keyIterator.next()
                iter.next() shouldBe keyIterator.next()
                iter.next() shouldBe keyIterator.next()
            }

            "when inner map is NOT MapIterable, return instance of Iterator" {
                val map = MockPersistentMap("a" to "1", "b" to "2", "c" to "3")
                val set = HashSet(map)

                val iter = set.iterator()

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe "a"
                iter.next() shouldBe "b"
                iter.next() shouldBe "c"
                iter.hasNext().shouldBeFalse()
            }
        }

        "isEmpty()" {
            val map1 = hashMap("a" to "1", "b" to "2", "c" to "3")
            val map2 = LeanMap<String, String>()

            HashSet(map1).isEmpty().shouldBeFalse()
            HashSet(map2).isEmpty().shouldBeTrue()
        }

        "containsAll(coll)" {
            val map = hashMap("a" to "1", "b" to "2", "c" to "3")
            val set = HashSet(map)

            set.containsAll(listOf("a", "c")).shouldBeTrue()
            set.containsAll(listOf("a", "c", "x")).shouldBeFalse()
        }
    }

    "EmptyHashSet" - {
        "map property should be set to EmptyLeanMap" {
            EmptyHashSet.map shouldBeSameInstanceAs EmptyLeanMap
        }

        "count should return 0" {
            EmptyHashSet.count shouldBeExactly 0
        }
    }

    "HashSet" - {
        "count" {
            val map = hashMap("a" to "1", "b" to "2", "c" to "3")

            val m = HashSet(map)

            m.count shouldBeExactly map.count
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
