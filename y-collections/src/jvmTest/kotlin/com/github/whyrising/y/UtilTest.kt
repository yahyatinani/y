package com.github.whyrising.y

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class UtilTest : FreeSpec({

    class UnsupportedNumber : Number() {
        override fun toByte(): Byte = 0

        override fun toChar(): Char = 'a'

        override fun toDouble(): Double = 0.0

        override fun toFloat(): Float = 0.0f

        override fun toInt(): Int = 0

        override fun toLong(): Long = 0

        override fun toShort(): Short = 0

        override fun toString(): String = "UnsupportedNumber"
    }

    "category(n:Number)" {
        val a: Byte = 1
        val b: Short = 1
        val c = 1
        val d: Long = 1
        val e = 1f
        val f = 1.0
        val unsupportedNum = UnsupportedNumber()

        category(a) shouldBe Category.INTEGER
        category(b) shouldBe Category.INTEGER
        category(c) shouldBe Category.INTEGER
        category(d) shouldBe Category.INTEGER
        category(e) shouldBe Category.FLOATING
        category(f) shouldBe Category.FLOATING

        val exception = shouldThrowExactly<IllegalStateException> {
            category(unsupportedNum)
        }
        exception.message shouldBe
            "The category of the number: $unsupportedNum is not supported"
    }

    "ops(n:Number)" {
        val a: Byte = 1
        val b: Short = 1
        val c = 1
        val d: Long = 1
        val e = 1f
        val f = 1.0
        val unsupportedNum = UnsupportedNumber()

        ops(a) shouldBe LongOps
        ops(b) shouldBe LongOps
        ops(c) shouldBe LongOps
        ops(d) shouldBe LongOps
        ops(e) shouldBe DoubleOps
        ops(f) shouldBe DoubleOps

        shouldThrowExactly<IllegalStateException> {
            ops(unsupportedNum)
        }.message shouldBe
            "The Ops of the number: $unsupportedNum is not supported"
    }

    "LongOps, DoubleOps" - {
        "combine(y: Ops)" {
            LongOps.combine(LongOps) shouldBe LongOps
            LongOps.combine(DoubleOps) shouldBe DoubleOps

            DoubleOps.combine(DoubleOps) shouldBe DoubleOps
            DoubleOps.combine(LongOps) shouldBe DoubleOps
        }

        "equiv(x: Number, y: Number)" {
            LongOps.equiv(1, 1L).shouldBeTrue()
            DoubleOps.equiv(1f, 1.0).shouldBeTrue()
        }
    }

    "toSeq(x)" - {
        "when x is null, it should return null" {
            toSeq<Int>(null).shouldBeNull()
        }

        "ASeq" {
            val x: Any = Cons(1, PersistentList.Empty)

            val seq = toSeq<Int>(x) as ISeq<Int>

            seq.first() shouldBeExactly 1
        }

        "Seqable" {
            val x: Any = v(1, 2)

            val seq = toSeq<Int>(x) as ISeq<Int>

            seq.first() shouldBeExactly 1
        }

        "not supported" {
            val x: Any = true

            val e = shouldThrowExactly<IllegalArgumentException> {
                toSeq<Int>(x)
            }

            e.message shouldBe
                "Don't know how to create ISeq from: ${x::class.simpleName}"
        }
    }
})
