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

        @Suppress("UNCHECKED_CAST")
        "conj(entry)" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            "when entry is a Map.Entry, it should call assoc() on it" {
                val newMap = map.conj(MapEntry("a", 99))
                    as ArrayMap<String, Int>

                newMap.count shouldBeExactly array.size
                newMap.array[0].second shouldBeExactly 99
                newMap.array[1].second shouldBeExactly 2
                newMap.array[2].second shouldBeExactly 3
            }

            "when entry is a IPersistentVector" - {
                "when count != 2, it should throw" {
                    shouldThrowExactly<IllegalArgumentException> {
                        map.conj(v("a", 99, 75))
                    }.message shouldBe
                        "Vector [a 99 75] count should be 2 to conj in a map"
                }

                "when count == 2, it should call assoc() on it" {
                    val newMap = map.conj(v("a", 99))
                        as ArrayMap<String, Int>

                    newMap.count shouldBeExactly array.size
                    newMap.array[0].second shouldBeExactly 99
                    newMap.array[1].second shouldBeExactly 2
                    newMap.array[2].second shouldBeExactly 3
                }
            }

            "when entry is null, it should return this" {
                map.conj(null) shouldBeSameInstanceAs map
            }

            "when entry is a seq of MapEntry" - {
                "when an element is not a MapEntry, it should throw" {
                    shouldThrowExactly<IllegalArgumentException> {
                        map.conj(l(MapEntry("x", 42), "item"))
                    }.message shouldBe
                        "All elements of the seq must be of type Map.Entry" +
                        " to conj: item"
                }

                "when all elements are MapEntry, it should assoc() all" {
                    val entries = l(MapEntry("x", 42), MapEntry("y", 47))

                    val newMap = map.conj(entries) as ArrayMap<String, Int>

                    newMap.count shouldBeExactly array.size + entries.count
                }
            }
        }

        "equiv(other)" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            "when other is not a Map, it should return false" {
                map.equiv("map").shouldBeFalse()
            }

            "when other is Map but different sizes, it should return false" {
                map.equiv(mapOf("a" to 1)).shouldBeFalse()
            }

            "when maps have same size but not equiv, it should return false" {
                map.equiv(mapOf("a" to 1, "b" to 7, "c" to 3)).shouldBeFalse()
                map.equiv(mapOf("a" to 1, "x" to 7, "c" to 3)).shouldBeFalse()
            }

            "when maps have same size and are equiv, return true" {
                map.equiv(mapOf("a" to 1L, "b" to 2, "c" to 3L)).shouldBeTrue()
            }

            "when other is IPersistentMap but not marked, return false" {
                val other = MockPersistentMap("a" to 1L, "b" to 2, "c" to 3L)

                map.equiv(other).shouldBeFalse()
            }
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

        "toString()" {
            am<String, Int>().toString() shouldBe "{}"
            am("a" to 1).toString() shouldBe "{a 1}"
            am("a" to 1, "b" to 2).toString() shouldBe "{a 1, b 2}"
            am("a" to 1, "b" to 2, "c" to 3).toString() shouldBe "{a 1, b 2, c 3}"
        }

        "hashCode()" {
            val array = arrayOf("a" to 1, "b" to 2)
            val map = am(*array)
            val expHash = ("a".hashCode() xor 1.hashCode()) +
                ("b".hashCode() xor 2.hashCode())

            map.hashCode() shouldBeExactly expHash
            map._hashCode shouldBeExactly expHash
        }

        "equals(other)" {
            (am("a" to 1, "b" to 2) == am("a" to 1, "b" to 2)).shouldBeTrue()

            (am("a" to 1, "b" to 2).equals("string")).shouldBeFalse()

            (am("a" to 1, "b" to 2) == am("a" to 1)).shouldBeFalse()

            (am("a" to 1, "b" to 2) == am("a" to 1, "x" to 2)).shouldBeFalse()

            (am("a" to 1, "b" to 2) == am("a" to 1, "b" to 10)).shouldBeFalse()

            (am("a" to 1, "b" to 2) == am("a" to 1, "b" to 2L)).shouldBeFalse()
        }

        "invoke() operator" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)

            "invoke(key, default)" {
                map("a", -1) shouldBe 1
                map("z", -1) shouldBe -1
            }

            "invoke(key)" {
                map("a") shouldBe 1
                map("z").shouldBeNull()
            }
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

        "Map implementation" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val map = am(*array)
            val emptyMap = am<String, Int>()

            "size()" {
                map.size shouldBeExactly array.size
            }

            "isEmpty()" {
                map.isEmpty().shouldBeFalse()
                emptyMap.isEmpty().shouldBeTrue()
            }

            "containsValue(value)" {
                map.containsValue(1).shouldBeTrue()
                map.containsValue(3).shouldBeTrue()
                map.containsValue(10).shouldBeFalse()
                map.containsValue(40).shouldBeFalse()
            }

            "get(key)" {
                map["a"] shouldBe 1
                map["x"].shouldBeNull()
            }

            "keys should return an instance of AbstractSet" {
                val keys = map.keys as AbstractSet<String>
                val iterator = keys.iterator()

                keys.size shouldBeExactly array.size

                keys.contains("a").shouldBeTrue()
                keys.contains("x").shouldBeFalse()

                iterator.hasNext().shouldBeTrue()
                iterator.next() shouldBe "a"
                iterator.next() shouldBe "b"
                iterator.next() shouldBe "c"
                iterator.hasNext().shouldBeFalse()
                shouldThrowExactly<NoSuchElementException> {
                    iterator.next()
                }
            }

            "values should return an instance of AbstractCollection" {
                val values = map.values as AbstractCollection<Int>
                val iterator = values.iterator()

                values.size shouldBeExactly array.size

                iterator.hasNext().shouldBeTrue()
                iterator.next() shouldBe 1
                iterator.next() shouldBe 2
                iterator.next() shouldBe 3
                iterator.hasNext().shouldBeFalse()
                shouldThrowExactly<NoSuchElementException> {
                    iterator.next()
                }
            }

            "entries should return an instance of AbstractSet" {
                val entries = map.entries as AbstractSet<Entry<String, Number>>
                val iterator = entries.iterator()

                entries.size shouldBeExactly array.size

                @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
                entries.contains("x").shouldBeFalse()
                entries.contains(MapEntry("x", 1)).shouldBeFalse()
                entries.contains(MapEntry("a", 1)).shouldBeTrue()

                iterator.hasNext().shouldBeTrue()
                iterator.next() shouldBe MapEntry("a", 1)
                iterator.next() shouldBe MapEntry("b", 2)
                iterator.next() shouldBe MapEntry("c", 3)
                iterator.hasNext().shouldBeFalse()
                shouldThrowExactly<NoSuchElementException> {
                    iterator.next()
                }

                entries.hashCode() shouldBeExactly map.hashCode()
            }
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
