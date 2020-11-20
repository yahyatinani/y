package com.github.whyrising.y

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ArrayChunkTest : FreeSpec({
    "ctor" - {
        val a = arrayOf(1, 2, 3)
        "primary" {
            val arrayChunk = ArrayChunk(a, 0, a.size)

            arrayChunk.array shouldBeSameInstanceAs a
            arrayChunk.start shouldBeExactly 0
            arrayChunk.end shouldBeExactly a.size
        }

        "second ctor" {
            val arrayChunk = ArrayChunk(a)

            arrayChunk.array shouldBeSameInstanceAs a
            arrayChunk.start shouldBeExactly 0
            arrayChunk.end shouldBeExactly a.size
        }
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

    "count property" {
        val a = arrayOf(1, 2, 3)
        val arrayChunk = ArrayChunk(a, 0, a.size)

        arrayChunk.count shouldBeExactly a.size
        arrayChunk.dropFirst().count shouldBeExactly a.size - 1
        arrayChunk.dropFirst().dropFirst().count shouldBeExactly a.size - 2
    }

    "nth(index, default)" {
        val default = -1
        val a = arrayOf(55, 85, 96)
        val arrayChunk = ArrayChunk(a, 0, a.size)

        arrayChunk.nth(8, default) shouldBeExactly default
        arrayChunk.nth(-8, default) shouldBeExactly default
        arrayChunk.nth(0, default) shouldBeExactly 55
        arrayChunk.nth(1, default) shouldBeExactly 85
        arrayChunk.nth(2, default) shouldBeExactly 96
        arrayChunk.dropFirst().nth(2, default) shouldBeExactly default
        arrayChunk.dropFirst().nth(0, default) shouldBeExactly 85
        arrayChunk.dropFirst().nth(1, default) shouldBeExactly 96
    }

    "nth(index)" {
        val a = arrayOf(55, 85, 96)
        val arrayChunk = ArrayChunk(a, 0, a.size)

        arrayChunk.nth(0) shouldBeExactly 55
        arrayChunk.nth(1) shouldBeExactly 85
        arrayChunk.nth(2) shouldBeExactly 96

        arrayChunk.dropFirst().nth(0) shouldBeExactly 85
        arrayChunk.dropFirst().nth(1) shouldBeExactly 96

        shouldThrowExactly<ArrayIndexOutOfBoundsException> {
            arrayChunk.nth(10)
        }

        shouldThrowExactly<ArrayIndexOutOfBoundsException> {
            arrayChunk.dropFirst().nth(2)
        }
    }
})
