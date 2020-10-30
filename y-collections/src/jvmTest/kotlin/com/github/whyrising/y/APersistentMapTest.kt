package com.github.whyrising.y

import com.github.whyrising.y.APersistentMap.KeySeq
import com.github.whyrising.y.APersistentMap.ValSeq
import com.github.whyrising.y.PersistentArrayMap.Iter
import com.github.whyrising.y.mocks.MockPersistentMap
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeInstanceOf

@Suppress("UNCHECKED_CAST")
class APersistentMapTest : FreeSpec({
    "KeySeq" - {
        val map = am("a" to 1, "b" to 2, "c" to 3)

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
        val map = am("a" to 1, "b" to 2, "c" to 3)

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

    "IPersistentMap.keys()" {
        val map = am("a" to 1, "b" to 2, "c" to 3)

        val keys: ISeq<String> = map.keyz()

        keys shouldBe l("a", "b", "c")
    }

    "IPersistentMap.vals()" {
        val map = am("a" to 1, "b" to 2, "c" to 3)

        val vals: ISeq<Int> = map.vals()

        vals shouldBe l(1, 2, 3)
    }
})
