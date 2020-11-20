package com.github.whyrising.y

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ArrayChunkTest : FreeSpec({
    "ctor" {
        val a = arrayOf(1, 2, 3)
        val arrayChunk = ArrayChunk(a, 0, a.size)

        arrayChunk.array shouldBeSameInstanceAs a
        arrayChunk.start shouldBeExactly 0
        arrayChunk.end shouldBeExactly a.size
    }

    "dropFirst()" - {
        "while start < end, it should return a new ArrayChunk and inc start" {
            val a = arrayOf(1, 2, 3)
            val arrayChunk = ArrayChunk(a, 0, a.size)

            val newArrayChunk = arrayChunk.dropFirst() as ArrayChunk<Int>

            newArrayChunk.array shouldBeSameInstanceAs a
            newArrayChunk.start shouldBeExactly arrayChunk.start + 1
            newArrayChunk.end shouldBeExactly arrayChunk.end
        }

        "when start == end, it should throw an exception" {
            val a = arrayOf(1, 2, 3)
            val arrayChunk = ArrayChunk(a, 0, a.size)

            shouldThrowExactly<IllegalStateException> {
                arrayChunk.dropFirst()
                    .dropFirst()
                    .dropFirst()
                    .dropFirst()
            }
        }
    }
})
