package com.github.whyrising.y

import com.github.whyrising.y.PersistentArrayMap.ArrayMap
import com.github.whyrising.y.PersistentArrayMap.EmptyArrayMap
import com.github.whyrising.y.PersistentList.Empty
import com.github.whyrising.y.mocks.MockPersistentMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.collections.Map.Entry

internal fun <K, V> am(vararg pairs: Pair<K, V>) = PersistentArrayMap(*pairs)

class PersistentArrayMapTest : FreeSpec({
    "ArrayMap" - {
        "invoke() should return EmptyArrayMap" {
            val emptyMap = am<String, Int>()

            emptyMap shouldBeSameInstanceAs EmptyArrayMap
            emptyMap.array.size shouldBeExactly 0
        }

        "invoke(pairs)" - {
            "when pairs is empty, it should return EmptyMap" {
                val array = arrayOf<Pair<String, Int>>()

                am(*array) shouldBeSameInstanceAs EmptyArrayMap
            }

            "it should return an ArrayMap" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

                val map = am(*array)
                val pairs = map.array

                pairs shouldBe array
            }

            "when duplicate keys, it should throw an exception" {
                shouldThrowExactly<IllegalArgumentException> {
                    am("a" to 1, "b" to 2, "b" to 3)
                }
                shouldThrowExactly<IllegalArgumentException> {
                    am("a" to 1, "a" to 2, "b" to 3)
                }
                shouldThrowExactly<IllegalArgumentException> {
                    am("a" to 1, "b" to 2, "a" to 3)
                }

                shouldThrowExactly<IllegalArgumentException> {
                    am(1L to "a", 1 to "b")
                }
            }
        }

        "assoc(key, val)" - {
            "when map is empty, it should add the new entry" {
                val map = am<String, Int>()

                val newMap = map.assoc("a", 1) as ArrayMap<String, Int>
                val pairs = newMap.pairs

                pairs[0].first shouldBe "a"
                pairs[0].second shouldBe 1
            }

            "when the key is new, it should add it to the map" - {
                "when size < threshold, it should return a PersistentArrayMap" {
                    val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                    val map = am(*array)

                    val newMap = map.assoc("d", 4) as ArrayMap<String, Int>
                    val pairs = newMap.pairs

                    pairs[0].first shouldBe "a"
                    pairs[0].second shouldBe 1

                    pairs[3].first shouldBe "d"
                    pairs[3].second shouldBe 4
                }

                "when size >= THRESHOLD, it should return PersistentHashMap" {
                    // TODO : when PersistentHashMap is  implemented
                }
            }

            """when map already has the key and different value,
               it should replace it in a new map""" {
                val key = 2
                val value = "78"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                val newMap = map.assoc(key, value) as ArrayMap<Any, String>
                val pairs = newMap.pairs

                pairs.size shouldBeExactly array.size

                array[1].first shouldBe key
                array[1].second shouldBe "2"

                pairs[0].first shouldBe 1L
                pairs[0].second shouldBe "1"

                pairs[1].first shouldBe key
                pairs[1].second shouldBe value

                pairs[2].first shouldBe 3
                pairs[2].second shouldBe "3"
            }

            """when map already has the key and same value,
               it should return the same map""" {
                val key = 2
                val value = "2"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                val newMap = map.assoc(key, value) as ArrayMap<Any, String>

                newMap shouldBeSameInstanceAs map
            }
        }

        "assocNew(key, val)" - {
            "when map already has the key, it should throw" {
                val value = "78"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                shouldThrowExactly<RuntimeException> {
                    map.assocNew(2, value)
                }.message shouldBe "The key 2 is already present."
            }

            "when new key, it should add the association to the new map" {
                val key = 4
                val value = "4"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                val newMap = map.assocNew(key, value) as ArrayMap<Any, String>
                val pairs = newMap.pairs

                pairs.size shouldBeExactly array.size + 1

                pairs[0].first shouldBe 1L
                pairs[0].second shouldBe "1"

                pairs[2].first shouldBe 3
                pairs[2].second shouldBe "3"

                pairs[array.size].first shouldBe key
                pairs[array.size].second shouldBe value
            }

            "when size >= THRESHOLD, it should return PersistentHashMap" {
                // TODO : when PersistentHashMap is  implemented
            }
        }

        "dissoc(key)" - {
            "when key doesn't exit, it should return the same instance" {
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                map.dissoc(9) shouldBeSameInstanceAs map
            }

            "when key exists and size is 1, it should return the empty map" {
                val map = am(2L to "2")

                map.dissoc(2) shouldBeSameInstanceAs EmptyArrayMap
            }

            "when key exists, it should return a new map without that key" {
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                val newMap = map.dissoc(2) as PersistentArrayMap<Any?, String>
                val pairs = newMap.array

                pairs.size shouldBeExactly array.size - 1
                pairs[0] shouldBe array[0]
                pairs[1] shouldBe array[2]
            }
        }

        "containsKey(key)" {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            map.containsKey("a").shouldBeTrue()
            map.containsKey("b").shouldBeTrue()

            map.containsKey("d").shouldBeFalse()
        }

        "entryAt(key)" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            "when key doesn't exit, it should return null" {
                map.entryAt("d").shouldBeNull()
            }

            "when key does exist, it should return a MapEntry" {
                val mapEntry = map.entryAt("a") as MapEntry<String, Int>

                mapEntry.key shouldBe "a"
                mapEntry.value shouldBe 1
            }
        }

        "valAt(key, default)" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            "when key exists, it should return the assoc value" {
                map.valAt("a", -1) shouldBe 1
            }

            "when key doesn't exist, it should return the default value" {
                map.valAt("z", -1) shouldBe -1
            }
        }

        "valAt(key)" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            "when key exists, it should return the assoc value" {
                map.valAt("a") shouldBe 1
            }

            "when key doesn't exist, it should return the default value" {
                map.valAt("z").shouldBeNull()
            }
        }

        "seq()" - {
            "when map is empty, it should return an empty seq" {
                am<String, Int>().seq() shouldBeSameInstanceAs
                    Empty
            }

            "when map is populated, it should return a seq of entries" {
                val array = arrayOf("a" to 1)
                val map = am(*array)

                val seq = map.seq()
                val rest = seq.rest()

                seq.toString() shouldBe "([a 1])"

                seq.count shouldBeExactly map.size

                seq.first() shouldBe MapEntry("a", 1)

                rest shouldBeSameInstanceAs Empty
            }
        }

        "count()" {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

            am<String, Int>().count shouldBeExactly 0
            am(*array).count shouldBeExactly array.size
        }

        "empty()" {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

            am(*array).empty() shouldBeSameInstanceAs
                EmptyArrayMap
        }

        "iterator()" {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)
            val iter = map.iterator()

            iter.hasNext().shouldBeTrue()

            iter.next() shouldBe MapEntry("a", 1)
            iter.next() shouldBe MapEntry("b", 2)
            iter.next() shouldBe MapEntry("c", 3)

            iter.hasNext().shouldBeFalse()

            shouldThrowExactly<NoSuchElementException> { iter.next() }
        }

        "keyIterator()" {
            val map = am("a" to 1, "b" to 2, "c" to 3)

            val iter: Iterator<String> = map.keyIterator()

            iter.hasNext().shouldBeTrue()

            iter.next() shouldBe "a"
            iter.next() shouldBe "b"
            iter.next() shouldBe "c"

            iter.hasNext().shouldBeFalse()

            shouldThrowExactly<NoSuchElementException> { iter.next() }
        }

        "valIterator()" {
            val map = am("a" to 1, "b" to 2, "c" to 3)

            val iter: Iterator<Int> = map.valIterator()

            iter.hasNext().shouldBeTrue()

            iter.next() shouldBeExactly 1
            iter.next() shouldBeExactly 2
            iter.next() shouldBeExactly 3

            iter.hasNext().shouldBeFalse()

            shouldThrowExactly<NoSuchElementException> { iter.next() }
        }
    }

    "EmptyArrayMap" - {
        "toString() should return `{}`" {
            am<String, Int>().toString() shouldBe "{}"
        }

        "hashCode()" {
            am<String, Int>().hashCode() shouldBeExactly 0
        }
    }
})
