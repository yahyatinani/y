package com.github.whyrising.y

import com.github.whyrising.y.PersistentList.Empty
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.property.checkAll

class SeqIteratorTest : FreeSpec({
    "hasNext()" - {
        "should return false when the list is Empty" {
            val list: PersistentList<Int> = Empty

            SeqIterator(list).hasNext().shouldBeFalse()
        }

        "should return true when the list is populated" {
            val list = PersistentList.Cons(1, Empty)

            SeqIterator(list).hasNext().shouldBeTrue()
        }
    }

    "next()" - {
        "when the list is empty, it should throw NoSuchElementException" {
            val list: PersistentList<Int> = Empty

            shouldThrowExactly<NoSuchElementException> {
                SeqIterator(list).next()
            }
        }

        "when the list is populated, it should return the next element" {
            checkAll { ints: List<Int> ->
                val list = PersistentList(*ints.toTypedArray())
                val seqIterator = SeqIterator(list)

                var index = 0
                while (seqIterator.hasNext()) {
                    val next = seqIterator.next()
                    next.shouldNotBeNull()
                    next shouldBeExactly ints[index]

                    index++
                }
            }
        }
    }
})
