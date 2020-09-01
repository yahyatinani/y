package com.github.whyrising.y.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll
import java.math.BigInteger

class CoreTest : FreeSpec({
    "identity" - {

        "for Int" {
            forAll(Arb.int()) { n: Int ->
                val r = identity(n)

                r == n
            }
        }

        "for Double" {
            forAll(Arb.double()) { d: Double ->
                val r = identity(d)

                r.equals(d)
            }
        }

        "for String" {
            forAll(Arb.string()) { s: String ->
                val r = identity(s)

                r == s
            }
        }

        "for Boolean" {
            forAll(Arb.bool()) { b: Boolean ->
                val r = identity(b)

                r == b
            }
        }

        "for Char" {
            forAll(Arb.char()) { c: Char ->
                val r = identity(c)

                r == c
            }
        }

        "for List" {
            forAll(Arb.list(Arb.double())) { list: List<Double> ->
                val r = identity(list)

                r == list
            }
        }

        "for value function" {
            val f = {}

            val r = identity(f)

            r shouldBe f
        }
    }

    "inc" - {

        "Increment Bytes" {
            forAll(Arb.byte()) { n: Byte ->
                val r: Byte = inc(n)

                r == n.inc()
            }
        }

        "Increment Shorts" {
            forAll(Arb.short()) { n: Short ->
                val r: Short = inc(n)

                r == n.inc()
            }
        }

        "Increment Integers" {
            forAll(Arb.int()) { n: Int ->
                val r: Int = inc(n)

                r == n.inc()
            }
        }

        "Increment Longs" {
            forAll(Arb.long()) { n: Long ->
                val r: Long = inc(n)

                r == n.inc()
            }
        }

        "Increment BigIntegers" {
            forAll(Arb.bigInt(45)) { n: BigInteger ->
                val r: BigInteger = inc(n)

                r == n.inc()
            }
        }

        "Increment Floats" {
            forAll(Arb.float()) { n: Float ->
                val r: Float = inc(n)

                r.equals(n.inc())
            }
        }

        "Increment Doubles" {
            forAll(Arb.double()) { n: Double ->
                val r: Double = inc(n)

                r.equals(n.inc())
            }
        }
    }

    "dec" - {
        "Decrement Bytes" {
            forAll(Arb.byte()) { n: Byte ->
                val r: Byte = dec(n)

                r == n.dec()
            }
        }

        "Decrement Shorts" {
            forAll(Arb.short()) { n: Short ->
                val r: Short = dec(n)

                r == n.dec()
            }
        }

        "Decrement Integers" {
            forAll(Arb.int()) { n: Int ->
                val r: Int = dec(n)

                r == n.dec()
            }
        }

        "Decrement Longs" {
            forAll(Arb.long()) { n: Long ->
                val r: Long = dec(n)

                r == n.dec()
            }
        }

        "Decrement BigIntegers" {
            forAll(Arb.bigInt(45)) { n: BigInteger ->
                val r: BigInteger = dec(n)

                r == n.dec()
            }
        }

        "Decrement Floats" {
            forAll(Arb.float()) { n: Float ->
                val r: Float = dec(n)

                r.equals(n.dec())
            }
        }

        "Decrement Doubles" {
            forAll(Arb.double()) { n: Double ->
                val r: Double = dec(n)

                r.equals(n.dec())
            }
        }
    }

    "str" - {
        "When passing one argument." - {
            "When passing `null` It should return the empty string." {
                forAll(Arb.string().map { null }) { nil ->
                    val r = str(nil)

                    r == ""
                }
            }

            "When passing `arg` It should return arg.toString()." {
                forAll(Arb.string()) { s: String ->
                    val r = str(s)

                    r == s
                }

                forAll(Arb.int()) { i: Int ->
                    str(i) == i.toString()
                }
            }
        }

        "When passing multiple arguments" - {
            "It returns the concatenation of two strings" {
                checkAll { a: String, b: String ->
                    val r = str(a, b)

                    r shouldBe "$a$b"
                }
            }

            "It returns the concatenation of three strings" {
                checkAll { a: String, b: String, c: String ->
                    val r = str(a, b, c)

                    r shouldBe "$a$b$c"
                }
            }

            "It returns the concatenation of four strings" {
                checkAll { a: String, b: String, c: String, d: String ->
                    val r = str(a, b, c, d)

                    r shouldBe "$a$b$c$d"
                }
            }
        }
    }
})
