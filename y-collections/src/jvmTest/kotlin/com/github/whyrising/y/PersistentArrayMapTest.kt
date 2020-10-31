package com.github.whyrising.y

import com.github.whyrising.y.PersistentArrayMap.ArrayMap
import com.github.whyrising.y.PersistentArrayMap.EmptyArrayMap
import com.github.whyrising.y.PersistentArrayMap.TransientArrayMap
import com.github.whyrising.y.PersistentList.Empty
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

internal fun <K, V> am(vararg pairs: Pair<K, V>) = PersistentArrayMap(*pairs)

class PersistentArrayMapTest : FreeSpec({
    "TransientArrayMap" - {
        "ctor" {
            val gen = Arb.list(Arb.pair(Arb.string(), Arb.int()))
            checkAll(gen) { list: List<Pair<String, Int>> ->
                val array = list.toTypedArray()

                val tam = TransientArrayMap(array)

                tam.array shouldNotBeSameInstanceAs array
                when {
                    list.size <= HASHTABLE_THRESHOLD -> {
                        tam.array.size shouldBeExactly HASHTABLE_THRESHOLD
                    }
                    else -> {
                        tam.array.size shouldBeExactly list.size
                    }
                }
                tam.length.value shouldBeExactly list.size
                tam.isMutable.value.shouldBeTrue()
                shouldNotThrow<Exception> { tam.assertMutable() }
            }
        }

        "doPersistent()" {
            val array = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
            val tam = TransientArrayMap(array)

            val map = tam.doPersistent() as PersistentArrayMap<String, Int>

            tam.isMutable.value.shouldBeFalse()
            map.count shouldBeExactly array.size
            map.array shouldNotBeSameInstanceAs array
            map shouldBe am("a" to 1, "b" to 2, "c" to 3)

            shouldThrowExactly<IllegalStateException> {
                tam.doPersistent()
            }.message shouldBe "Transient used after persistent() call."
        }

        "persistent()" {
            val array = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
            val tam = TransientArrayMap(array)

            val map = tam.persistent() as PersistentArrayMap<String, Int>

            tam.isMutable.value.shouldBeFalse()
            map.count shouldBeExactly array.size
            map.array shouldNotBeSameInstanceAs array
            map shouldBe am("a" to 1, "b" to 2, "c" to 3)

            shouldThrowExactly<IllegalStateException> {
                tam.persistent()
            }.message shouldBe "Transient used after persistent() call."
        }

        "doAssoc(k,v)" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.doAssoc("x", 99)
                }.message shouldBe "Transient used after persistent() call."
            }

            "when the key is new, it should add it to the map" - {
                "when length < array.length, return a TransientArrayMap" {
                    val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                    val tam = TransientArrayMap(array)

                    val newTam = tam.doAssoc("d", 4) as
                        TransientArrayMap<String, Int>
                    val pairs = newTam.array

                    pairs[0]!!.first shouldBe "a"
                    pairs[0]!!.second shouldBe 1

                    pairs[3]!!.first shouldBe "d"
                    pairs[3]!!.second shouldBe 4
                }

                "when length >= array.length, return PersistentHashMap" {
                    // TODO : when PersistentHashMap is  implemented
                }
            }

            """when map already has the key but different value,
                   it should update the value""" {
                val key = 2
                val value = "78"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val tam = TransientArrayMap(array)

                val newTam = tam.doAssoc(key, value)
                    as TransientArrayMap<Any, String>
                val pairs = newTam.array

                tam.length.value shouldBeExactly array.size

                pairs[0]!!.first shouldBe 1L
                pairs[0]!!.second shouldBe "1"

                pairs[1]!!.first shouldBe key
                pairs[1]!!.second shouldBe value

                pairs[2]!!.first shouldBe 3
                pairs[2]!!.second shouldBe "3"
            }

            """when map already has the same key/value,
                   it should return the same map""" {
                val key = 2
                val value = "2"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val tam = TransientArrayMap(array)

                val newTam = tam.doAssoc(key, value)
                    as TransientArrayMap<Any, String>
                val pairs = newTam.array

                tam shouldBeSameInstanceAs newTam
                pairs[1]!!.first.shouldBeInstanceOf<Long>()
            }
        }

        "assoc(k,v)" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.assoc("x", 99)
                }.message shouldBe "Transient used after persistent() call."
            }

            "when the key is new, it should add it to the map" - {
                "when length < array.length, return a TransientArrayMap" {
                    val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                    val tam = TransientArrayMap(array)

                    val newTam = tam.assoc("d", 4) as
                        TransientArrayMap<String, Int>
                    val pairs = newTam.array

                    pairs[0]!!.first shouldBe "a"
                    pairs[0]!!.second shouldBe 1

                    pairs[3]!!.first shouldBe "d"
                    pairs[3]!!.second shouldBe 4
                }

                "when length >= array.length, return PersistentHashMap" {
                    // TODO : when PersistentHashMap is  implemented
                }
            }

            """when map already has the key but different value,
                   it should update the value""" {
                val key = 2
                val value = "78"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val tam = TransientArrayMap(array)

                val newTam = tam.assoc(key, value)
                    as TransientArrayMap<Any, String>
                val pairs = newTam.array

                tam.length.value shouldBeExactly array.size

                pairs[0]!!.first shouldBe 1L
                pairs[0]!!.second shouldBe "1"

                pairs[1]!!.first shouldBe key
                pairs[1]!!.second shouldBe value

                pairs[2]!!.first shouldBe 3
                pairs[2]!!.second shouldBe "3"
            }

            """when map already has the same key/value,
                   it should return the same map""" {
                val key = 2
                val value = "2"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val tam = TransientArrayMap(array)

                val newTam = tam.assoc(key, value)
                    as TransientArrayMap<Any, String>
                val pairs = newTam.array

                tam shouldBeSameInstanceAs newTam
                pairs[1]!!.first.shouldBeInstanceOf<Long>()
            }
        }

        @Suppress("UNCHECKED_CAST")
        "conj(e)" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.conj(Pair("x", 99))
                }.message shouldBe "Transient used after persistent() call."
            }

            "when entry is a Map.Entry, it should call assoc() on it" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                val newTam = tam.conj(MapEntry("a", 99))
                    as TransientArrayMap<String, Int>

                newTam.length.value shouldBeExactly a.size
                newTam.array[0]!!.second shouldBeExactly 99
                newTam.array[1]!!.second shouldBeExactly 2
                newTam.array[2]!!.second shouldBeExactly 3
            }

            "when entry is a IPersistentVector" - {
                "when count != 2, it should throw" {
                    val a = arrayOf(Pair("a", 1), Pair("b", 2))
                    val tam = TransientArrayMap(a)

                    shouldThrowExactly<IllegalArgumentException> {
                        tam.conj(v("a", 99, 75))
                    }.message shouldBe
                        "Vector [a 99 75] count must be 2 to conj in a map."
                }

                "when count == 2, it should call assoc() on it" {
                    val a = arrayOf(Pair("a", 1), Pair("b", 2))
                    val tam = TransientArrayMap(a)

                    val newMap = tam.conj(v("a", 99))
                        as TransientArrayMap<String, Int>

                    newMap.length.value shouldBeExactly a.size
                    newMap.array[0]!!.second shouldBeExactly 99
                    newMap.array[1]!!.second shouldBeExactly 2
                }
            }

            "when entry is null, it should return this" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2))
                val tam = TransientArrayMap(a)

                tam.conj(null) shouldBeSameInstanceAs tam
            }

            "when entry is a seq of MapEntry" - {
                "when an element is not a MapEntry, it should throw" {
                    val a = arrayOf(Pair("a", 1), Pair("b", 2))
                    val tam = TransientArrayMap(a)

                    shouldThrowExactly<IllegalArgumentException> {
                        tam.conj(l(MapEntry("x", 42), "item"))
                    }.message shouldBe
                        "All elements of the seq must be of type " +
                        "Map.Entry to conj: item"
                }

                "when all elements are MapEntry, it should assoc() all" {
                    val a = arrayOf(Pair("a", 1), Pair("b", 2))
                    val tam = TransientArrayMap(a)
                    val entries = l(MapEntry("x", 42), MapEntry("y", 47))

                    val newTam = tam.conj(entries)
                        as TransientArrayMap<String, Int>
                    val pairs = newTam.array

                    newTam.length.value shouldBeExactly
                        a.size + entries.count

                    pairs[0]!! shouldBe Pair("a", 1)
                    pairs[1]!! shouldBe Pair("b", 2)
                    pairs[2]!! shouldBe Pair("x", 42)
                    pairs[3]!! shouldBe Pair("y", 47)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        "doDissoc(key)" - {
            "when key doesn't exit, it should return the same instance" {
                val a = arrayOf(Pair(1, "a"), Pair(2, "b"), Pair(3, "c"))
                val tam = TransientArrayMap(a)

                val dissoc = tam.doDissoc(10)

                dissoc shouldBeSameInstanceAs tam
            }

            "when key exists and size is 1, it returns an empty transient" {
                val a = arrayOf(2L to "b")
                val tam = TransientArrayMap(a)

                val rTam = tam.doDissoc(2) as TransientArrayMap<Int, String>

                rTam.length.value shouldBeExactly 0
                val pairs = rTam.array

                for (i in pairs.indices)
                    pairs[i].shouldBeNull()
            }

            "when key exists, it should return a new map without that key" {
                val a = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val tam = TransientArrayMap(a)

                val rTam = tam.doDissoc(1) as TransientArrayMap<Any?, String>
                val pairs = rTam.array

                rTam.length.value shouldBeExactly a.size - 1
                pairs[0] shouldBe (3 to "3")
                pairs[1] shouldBe (2L to "2")
            }
        }

        @Suppress("UNCHECKED_CAST")
        "dissoc(key)" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.dissoc("a")
                }.message shouldBe "Transient used after persistent() call."
            }

            "when key doesn't exit, it should return the same instance" {
                val a = arrayOf(Pair(1, "a"), Pair(2, "b"), Pair(3, "c"))
                val tam = TransientArrayMap(a)

                val dissoc = tam.dissoc(10)

                dissoc shouldBeSameInstanceAs tam
            }

            "when key exists and size is 1, it returns an empty transient" {
                val a = arrayOf(2L to "b")
                val tam = TransientArrayMap(a)

                val rTam = tam.dissoc(2) as TransientArrayMap<Int, String>

                rTam.length.value shouldBeExactly 0
                val pairs = rTam.array

                for (i in pairs.indices)
                    pairs[i].shouldBeNull()
            }

            "when key exists, it should return a new map without that key" {
                val a = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val tam = TransientArrayMap(a)

                val rTam = tam.dissoc(1) as TransientArrayMap<Any?, String>
                val pairs = rTam.array

                rTam.length.value shouldBeExactly a.size - 1
                pairs[0] shouldBe (3 to "3")
                pairs[1] shouldBe (2L to "2")
            }
        }

        "doCount" {
            val a1: Array<Pair<Number, String>> = arrayOf()
            val a2: Array<Pair<Number, String>> =
                arrayOf(1L to "1", 2L to "2", 3 to "3")
            val tam = TransientArrayMap(a2).dissoc(1) as TransientArrayMap<*, *>

            TransientArrayMap(a1).doCount shouldBeExactly 0
            TransientArrayMap(a2).doCount shouldBeExactly a2.size
            tam.doCount shouldBeExactly 2
        }

        "count" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.count
                }.message shouldBe "Transient used after persistent() call."
            }

            "assertions" {
                val a1: Array<Pair<Number, String>> = arrayOf()
                val a2: Array<Pair<Number, String>> =
                    arrayOf(1L to "1", 2L to "2", 3 to "3")

                TransientArrayMap(a1).count shouldBeExactly 0
                TransientArrayMap(a2).count shouldBeExactly a2.size
                TransientArrayMap(a2).dissoc(1).count shouldBeExactly 2
            }
        }

        "doValAt(key, default)" - {
            val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
            val tam = TransientArrayMap(array)

            "when key exists, it should return the assoc value" {
                tam.doValAt("a", -1) shouldBe 1
            }

            "when key doesn't exist, it should return the default value" {
                tam.doValAt("z", -1) shouldBe -1
            }
        }

        "valAt(key, default)" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.valAt("a", -1)
                }.message shouldBe "Transient used after persistent() call."
            }

            "when key exists, it should return the assoc value" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                val tam = TransientArrayMap(array)

                tam.valAt("a", -1) shouldBe 1
            }

            "when key doesn't exist, it should return the default value" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                val tam = TransientArrayMap(array)

                tam.valAt("z", -1) shouldBe -1
            }
        }

        "valAt(key)" - {
            "when called after calling persistent, it should throw" {
                val a = arrayOf(Pair("a", 1), Pair("b", 2), Pair("c", 3))
                val tam = TransientArrayMap(a)

                tam.persistent()

                shouldThrowExactly<IllegalStateException> {
                    tam.valAt("a")
                }.message shouldBe "Transient used after persistent() call."
            }

            "when key exists, it should return the assoc value" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                val tam = TransientArrayMap(array)

                tam.valAt("a") shouldBe 1
            }

            "when key doesn't exist, it should return the default value" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
                val tam = TransientArrayMap(array)

                tam.valAt("z").shouldBeNull()
            }
        }
    }

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

            """when map already has the same key/value,
               it should return the same map""" {
                val key = 2
                val value = "2"
                val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
                val map = am(*array)

                val newMap = map.assoc(key, value) as ArrayMap<Any, String>
                val pairs = newMap.array

                newMap shouldBeSameInstanceAs map
                pairs[1].first.shouldBeInstanceOf<Long>()
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

        "count" {
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
