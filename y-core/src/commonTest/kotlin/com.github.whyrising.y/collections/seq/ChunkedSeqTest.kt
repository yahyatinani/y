package com.github.whyrising.y.collections.seq

import com.github.whyrising.y.collections.ArrayChunk
import com.github.whyrising.y.collections.Chunk
import com.github.whyrising.y.collections.concretions.list.ChunkedSeq
import com.github.whyrising.y.collections.concretions.list.PersistentList
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ChunkedSeqTest : FreeSpec({
    "ctor" {
        val chunk1 = ArrayChunk(arrayOf(1, 2, 3))
        val chunk2 = ArrayChunk(arrayOf(4, 5, 6))

        val seq = ChunkedSeq(chunk2)
        ChunkedSeq(chunk1, seq)
    }

    "firstChunk() should return the first chunk" {
        val chunk1 = ArrayChunk(arrayOf(1, 2, 3))
        val chunk2 = ArrayChunk(arrayOf(4, 5, 6))

        val seq = ChunkedSeq(chunk2)
        val chunkedSeq = ChunkedSeq(chunk1, seq)

        val firstChunk: Chunk<Int> = chunkedSeq.firstChunk()

        firstChunk shouldBeSameInstanceAs chunk1
    }

    "restChunks()" - {
        "when there is any, it should return them" {
            val chunk1 = ArrayChunk(arrayOf(1, 2, 3))
            val chunk2 = ArrayChunk(arrayOf(4, 5, 6))

            val seq = ChunkedSeq(chunk2)
            val chunkedSeq = ChunkedSeq(chunk1, seq)

            val rest: ISeq<Int> = chunkedSeq.restChunks()

            rest shouldBeSameInstanceAs seq
        }

        "when there is none, it should return the Empty seq" {
            val chunk = ArrayChunk(arrayOf(1, 2, 3))

            val chunkedSeq = ChunkedSeq(chunk)
            val rest: ISeq<Int> = chunkedSeq.restChunks()

            rest shouldBeSameInstanceAs PersistentList.Empty
        }
    }

    "first() should return the first element of the first chunk" {
        val chunk1 = ArrayChunk(arrayOf(1, 2, 3))
        val chunk2 = ArrayChunk(arrayOf(4, 5, 6))

        val seq = ChunkedSeq(chunk2)
        val chunkedSeq = ChunkedSeq(chunk1, seq)

        chunkedSeq.first() shouldBeExactly 1
        seq.first() shouldBeExactly 4
    }

    "rest()" - {
        "when firstChunk.count > 1, it should drop firstChunk's first element" {
            val chunk1 = ArrayChunk(arrayOf(1, 2, 3))
            val chunk2 = ArrayChunk(arrayOf(4, 5, 6))

            val seq = ChunkedSeq(chunk2)
            val chunkedSeq = ChunkedSeq(chunk1, seq)

            val rest = chunkedSeq.rest() as ChunkedSeq<Int>

            rest.first() shouldBeExactly 2
            rest.restChunks shouldBeSameInstanceAs seq
        }

        "when firstChunk.count <= 1, it should call restChunks()" {
            val chunk1 = ArrayChunk(arrayOf(1))
            val chunk2 = ArrayChunk(arrayOf(2))
            val seq = ChunkedSeq(chunk2)
            val chunkedSeq = ChunkedSeq(chunk1, seq)

            val rest: ISeq<Int> = chunkedSeq.rest()

            rest shouldBeSameInstanceAs seq
        }
    }
})
