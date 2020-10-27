package com.github.whyrising.y

import com.github.whyrising.y.PersistentArrayMap.ArrayMap
import com.github.whyrising.y.PersistentArrayMap.EmptyArrayMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ArrayMapTest : FreeSpec({
    "ArrayMap" - {
        "invoke() should return EmptyArrayMap" {
            val emptyMap = PersistentArrayMap<String, Int>()

            emptyMap shouldBeSameInstanceAs EmptyArrayMap
            emptyMap.array.size shouldBeExactly 0
        }

        "invoke(pairs)" - {
            "it should return an ArrayMap" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

                val map = PersistentArrayMap(*array)
                val pairs = map.array

                pairs shouldBe array
            }

            "when duplicate keys, it should throw an exception" {
                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap("a" to 1, "b" to 2, "b" to 3)
                }
                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap("a" to 1, "a" to 2, "b" to 3)
                }
                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap("a" to 1, "b" to 2, "a" to 3)
                }

                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap(1L to "a", 1 to "b")
                }
            }
        }

        "assoc(key, val)" - {
            "when map is empty, it should add the new entry" {
                val map = PersistentArrayMap<String, Int>()

                val newMap = map.assoc("a", 1) as ArrayMap<String, Int>
                val pairs = newMap.pairs

                pairs[0].first shouldBe "a"
                pairs[0].second shouldBe 1
            }

            "when the key is new, it should add it to the map" - {
                "when size < threshold, it should return a PersistentArrayMap" {
                    val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                    val map = PersistentArrayMap(*array)

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

            """when map already have the key and different value,
               it should replace it in a new map""" {
                val key = 2
                val value = "78"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = PersistentArrayMap(*array)

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

            """when map already have the key and same value,
               it should return the same map""" {
                val key = 2
                val value = "2"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = PersistentArrayMap(*array)

                val newMap = map.assoc(key, value) as ArrayMap<Any, String>

                newMap shouldBeSameInstanceAs map
            }
        }
    }

    "EmptyArrayMap" - {
        "toString() should return `{}`" {
            PersistentArrayMap<String, Int>().toString() shouldBe "{}"
        }
    }
})
