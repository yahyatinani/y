package com.github.whyrising.y.core

import io.kotest.core.spec.style.FunSpec
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
import io.kotest.property.forAll
import java.math.BigInteger

class CoreTest : FunSpec({
    context("identity") {

        test("for Int") {
            forAll(Arb.int()) { n: Int ->
                val r = identity(n)

                r == n
            }
        }

        test("for Double") {
            forAll(Arb.double()) { d: Double ->
                val r = identity(d)

                r.equals(d)
            }
        }

        test("for String") {
            forAll(Arb.string()) { s: String ->
                val r = identity(s)

                r == s
            }
        }

        test("for Boolean") {
            forAll(Arb.bool()) { b: Boolean ->
                val r = identity(b)

                r == b
            }
        }

        test("for Char") {
            forAll(Arb.char()) { c: Char ->
                val r = identity(c)

                r == c
            }
        }

        test("for List") {
            forAll(Arb.list(Arb.double())) { list: List<Double> ->
                val r = identity(list)

                r == list
            }
        }

        test("for value function") {
            val f = {}

            val r = identity(f)

            r shouldBe f
        }
    }

    context("inc") {

        test("Increment Bytes") {
            forAll(Arb.byte()) { n: Byte ->
                val r: Byte = inc(n)

                r == n.inc()
            }
        }

        test("Increment Shorts") {
            forAll(Arb.short()) { n: Short ->
                val r: Short = inc(n)

                r == n.inc()
            }
        }

        test("Increment Integers") {
            forAll(Arb.int()) { n: Int ->
                val r: Int = inc(n)

                r == n.inc()
            }
        }

        test("Increment Longs") {
            forAll(Arb.long()) { n: Long ->
                val r: Long = inc(n)

                r == n.inc()
            }
        }

        test("Increment BigIntegers") {
            forAll(Arb.bigInt(45)) { n: BigInteger ->
                val r: BigInteger = inc(n)

                r == n.inc()
            }
        }

        test("Increment Floats") {
            forAll(Arb.float()) { n: Float ->
                val r: Float = inc(n)

                r.equals(n.inc())
            }
        }

        test("Increment Doubles") {
            forAll(Arb.double()) { n: Double ->
                val r: Double = inc(n)

                r.equals(n.inc())
            }
        }
    }

    context("dec") {
        test("Decrement Bytes") {
            forAll(Arb.byte()) { n: Byte ->
                val r: Byte = dec(n)

                r == n.dec()
            }
        }

        test("Decrement Shorts") {
            forAll(Arb.short()) { n: Short ->
                val r: Short = dec(n)

                r == n.dec()
            }
        }

        test("Decrement Integers") {
            forAll(Arb.int()) { n: Int ->
                val r: Int = dec(n)

                r == n.dec()
            }
        }

        test("Decrement Longs") {
            forAll(Arb.long()) { n: Long ->
                val r: Long = dec(n)

                r == n.dec()
            }
        }

        test("Decrement BigIntegers") {
            forAll(Arb.bigInt(45)) { n: BigInteger ->
                val r: BigInteger = dec(n)

                r == n.dec()
            }
        }

        test("Decrement Floats") {
            forAll(Arb.float()) { n: Float ->
                val r: Float = dec(n)

                r.equals(n.dec())
            }
        }

        test("Decrement Doubles") {
            forAll(Arb.double()) { n: Double ->
                val r: Double = dec(n)

                r.equals(n.dec())
            }
        }
    }

    context("str") {
        test("Whn passing `null` It returns the empty string.") {
            forAll(Arb.string().map { null }) { nil ->
                val r = str(nil)

                r == ""
            }
        }

        test("When passing one arg, It should return arg.toString().") {
            forAll(Arb.string()) { s: String ->
                val r = str(s)

                r == s
            }

            forAll(Arb.int()) { i: Int ->
                str(i) == i.toString()
            }
        }
    }
})
