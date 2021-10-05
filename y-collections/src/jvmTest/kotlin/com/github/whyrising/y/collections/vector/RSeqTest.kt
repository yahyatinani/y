package com.github.whyrising.y.collections.vector

import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.vector.APersistentVector.RSeq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class RSeqTest : FreeSpec({
    "first" {
        checkAll(Arb.list(Arb.int(), 1..20)) { list: List<Int> ->
            val vec = PersistentVector(*list.toTypedArray())
            val lastIndex = list.size - 1

            val rSeq = RSeq(vec, lastIndex)

            rSeq.count shouldBeExactly vec.count
            rSeq.first() shouldBeExactly vec[lastIndex]
        }
    }

    "rest()" - {
        "when the index is 0, it should return the empty seq" {
            val vec = PersistentVector(1)
            val rseq = RSeq(vec, vec.size - 1)

            val rest = rseq.rest()

            rest shouldBeSameInstanceAs PersistentList.Empty
        }

        "when index > 0, it should return the rest of the reversed seq" {
            checkAll(Arb.list(Arb.int(), 2..20)) { list: List<Int> ->
                val vec = PersistentVector(*list.toTypedArray())
                val lastIndex = list.size - 1

                val rest = RSeq(vec, lastIndex).rest() as RSeq<Int>

                rest.index shouldBeExactly lastIndex - 1
                rest.count shouldBeExactly rest.index + 1
                rest.first() shouldBeExactly vec[lastIndex - 1]

                rest.rest().count shouldBeExactly rest.index
            }
        }
    }

    "next()" - {
        "when the index is 0, it should return the empty seq" {
            val vec = PersistentVector(1)
            val rseq = RSeq(vec, vec.size - 1)

            rseq.next().shouldBeNull()
        }

        "when index > 0, it should return the rest of the reversed seq" {
            checkAll(Arb.list(Arb.int(), 5..20)) { list: List<Int> ->
                val vec = PersistentVector(*list.toTypedArray())
                val lastIndex = list.size - 1

                val next = RSeq(vec, lastIndex).next() as RSeq<Int>

                next.index shouldBeExactly lastIndex - 1
                next.count shouldBeExactly next.index + 1
                next.first() shouldBeExactly vec[lastIndex - 1]
                next.next()!!.count shouldBeExactly next.index
            }
        }
    }
})
