package com.github.whyrising.y

import com.github.whyrising.y.APersistentMap.KeySeq
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
})
