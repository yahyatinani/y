package com.github.whyrising.y

import com.github.whyrising.y.APersistentMap.KeySeq
import com.github.whyrising.y.APersistentMap.ValSeq
import com.github.whyrising.y.PersistentArrayMap.Iter
import com.github.whyrising.y.mocks.MockPersistentMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeInstanceOf

@ExperimentalStdlibApi
@Suppress("UNCHECKED_CAST")
class APersistentMapTest : FreeSpec({
    "toString()" {
        m<String, Int>().toString() shouldBe "{}"
        m("a" to 1).toString() shouldBe "{a 1}"
        m("a" to 1, "b" to 2).toString() shouldBe "{a 1, b 2}"
        m("a" to 1, "b" to 2, "c" to 3).toString() shouldBe "{a 1, b 2, c 3}"
    }

    "hashCode()" {
        val array = arrayOf("a" to 1, "b" to 2)
        val map = m(*array)
        val expHash = ("a".hashCode() xor 1.hashCode()) +
            ("b".hashCode() xor 2.hashCode())

        map.hashCode() shouldBeExactly expHash
        map.hashCode shouldBeExactly expHash
    }

    "hasheq()" {
        val map = m("a" to 1, "b" to 2, "c" to 3)
        val expectedHash = Murmur3.hashUnordered(map)

        map.hasheq shouldBeExactly 0

        val hash = map.hasheq()

        hash shouldBeExactly expectedHash
        map.hasheq shouldBeExactly expectedHash
        m<String, Int>().hasheq() shouldBeExactly -15128758
        m<String, Int>().hasheq() shouldBeExactly
            hashMap<String, Int>().hasheq()
    }

    "equals(other)" {
        val m = m("a" to 1, "b" to 2)

        (m == m).shouldBeTrue()

        (m<String, Int>() == mapOf<String, Int>()).shouldBeTrue()

        (m("a" to 1, "b" to 2) == m("a" to 1, "b" to 2)).shouldBeTrue()

        (m("a" to 1, "b" to 2).equals("string")).shouldBeFalse()

        (m("a" to 1, "b" to 2) == m("a" to 1)).shouldBeFalse()

        (m("a" to 1, "b" to 2) == m("a" to 1, "x" to 2)).shouldBeFalse()

        (m("a" to 1, "b" to 2) == m("a" to 1, "b" to 10)).shouldBeFalse()

        (m("a" to 1, "b" to 2) == m("a" to 1, "b" to 2L)).shouldBeFalse()
    }

    @Suppress("UNCHECKED_CAST")
    "conj(entry)" - {
        val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
        val map = m(*array)

        "when entry is a Map.Entry, it should call assoc() on it" {
            val newMap = map.conj(MapEntry("a", 99))
                as PersistentArrayMap.ArrayMap<String, Int>

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
                    as PersistentArrayMap.ArrayMap<String, Int>

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

                val newMap = map.conj(entries) as PersistentArrayMap.ArrayMap<String, Int>

                newMap.count shouldBeExactly array.size + entries.count
            }
        }
    }

    "equiv(other)" - {
        val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
        val map = m(*array)

        m<String, Int>().equiv(mapOf<String, Int>()).shouldBeTrue()

        "when this and other are the same instance, it should return true" {
            map.equiv(map).shouldBeTrue()
        }

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

    "invoke() operator" - {
        val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
        val map = m(*array)

        "invoke(key, default)" {
            map("a", -1) shouldBe 1
            map("z", -1) shouldBe -1
        }

        "invoke(key)" {
            map("a") shouldBe 1
            map("z").shouldBeNull()
        }
    }

    "KeySeq" - {
        val map = m("a" to 1, "b" to 2, "c" to 3)

        "KeySeq should be a seq" {
            val keySeq: ISeq<String> = KeySeq(map)
            val rest = keySeq.rest() as KeySeq<String, Int>
            val seq: ISeq<String> = rest._seq

            seq.count shouldBeExactly rest.count
            keySeq.count shouldBeExactly map.size

            keySeq.first() shouldBe "a"

            rest.map.shouldBeNull()
            rest.count shouldBeExactly map.size - 1
            rest.first() shouldBe "b"
            rest.rest().first() shouldBe "c"
        }

        "iterator()" - {
            "when map is a MapIterable, it should return an instance of Iter" {
                val keySeq: ASeq<String> = KeySeq(map)
                val iter = keySeq.iterator() as Iter<String, Int, String>

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe "a"
                iter.next() shouldBe "b"
                iter.next() shouldBe "c"
                iter.hasNext().shouldBeFalse()
            }

            "when map is null, it should return an instance of SeqIterator" {
                val keySeq: ASeq<String> = KeySeq(map)
                val kSeq = keySeq.rest() as KeySeq<String, Int>

                val iter = kSeq.iterator() as SeqIterator<String>

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe "b"
                iter.next() shouldBe "c"
                iter.hasNext().shouldBeFalse()
            }

            "when map != null and != MapIterable, return a new Iterator" {
                val nonMiter = MockPersistentMap("a" to 1, "b" to 2, "c" to 3)
                val keySeq: ASeq<String> = KeySeq(nonMiter)

                val iter = keySeq.iterator()

                iter.shouldNotBeInstanceOf<SeqIterator<*>>()

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe "a"
                iter.next() shouldBe "b"
                iter.next() shouldBe "c"
                iter.hasNext().shouldBeFalse()
            }
        }
    }

    "ValSeq" - {
        val map = m("a" to 1, "b" to 2, "c" to 3)

        "ValSeq should be a seq" {
            val valSeq: ISeq<Int> = ValSeq(map)
            val rest = valSeq.rest() as ValSeq<String, Int>
            val seq: ISeq<Int> = rest._seq

            valSeq.count shouldBeExactly map.size
            seq.count shouldBeExactly rest.count

            valSeq.first() shouldBe 1

            rest.map.shouldBeNull()
            rest.count shouldBeExactly map.size - 1
            rest.first() shouldBe 2
            rest.rest().first() shouldBe 3
        }

        "iterator()" - {
            "when map is a MapIterable, it should return an instance of Iter" {
                val valSeq: ASeq<Int> = ValSeq(map)
                val iter = valSeq.iterator() as Iter<String, Int, String>

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe 1
                iter.next() shouldBe 2
                iter.next() shouldBe 3
                iter.hasNext().shouldBeFalse()
            }

            "when map is null, it should return an instance of SeqIterator" {
                val valSeq: ASeq<Int> = ValSeq(map)
                val vSeq = valSeq.rest() as ValSeq<String, Int>

                val iter = vSeq.iterator() as SeqIterator<String>

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe 2
                iter.next() shouldBe 3
                iter.hasNext().shouldBeFalse()
            }

            "when map != null and != MapIterable, return a new Iterator" {
                val nonMiter = MockPersistentMap("a" to 1, "b" to 2, "c" to 3)
                val valSeq: ASeq<Int> = ValSeq(nonMiter)

                val iter = valSeq.iterator()

                iter.shouldNotBeInstanceOf<SeqIterator<*>>()

                iter.hasNext().shouldBeTrue()
                iter.next() shouldBe 1
                iter.next() shouldBe 2
                iter.next() shouldBe 3
                iter.hasNext().shouldBeFalse()
            }
        }
    }

    "Map implementation" - {
        val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
        val map = m(*array)
        val emptyMap = m<String, Int>()

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
            val entries = map.entries as AbstractSet<Map.Entry<String, Number>>
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

    "IPersistentMap.keyz()" {
        val map = m("a" to 1, "b" to 2, "c" to 3)

        val keys: ISeq<String> = map.keyz()

        keys shouldBe l("a", "b", "c")
    }

    "IPersistentMap.vals()" {
        val map = m("a" to 1, "b" to 2, "c" to 3)

        val vals: ISeq<Int> = map.vals()

        vals shouldBe l(1, 2, 3)
    }
})
