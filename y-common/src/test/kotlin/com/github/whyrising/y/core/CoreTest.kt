package com.github.whyrising.y.core

import com.github.whyrising.y.`string?`
import com.github.whyrising.y.foo1
import com.github.whyrising.y.foo2
import com.github.whyrising.y.foo3
import com.github.whyrising.y.foo4
import com.github.whyrising.y.foo5
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
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
        "When passing no arguments, It should return the empty string." {
            val r = str()

            r shouldBe ""
        }

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

            "It should return the string concatenation of all args" {
                checkAll(Arb.list(Arb.int())) { list: List<Int> ->
                    val expected = list.fold("") { acc, i -> "$acc$i" }

                    val r = str(*list.toTypedArray())

                    r shouldBe expected
                }
            }

            "it should replace null with empty string and concat the rest" {
                checkAll(
                    Arb.`string?`(),
                    Arb.`string?`(),
                    Arb.`string?`()
                ) { a: String?, b: String?, c: String? ->
                    val expected = "${str(a)}${str(b)}${str(c)}"

                    val r = str(a, b, c)

                    r shouldBe expected
                }
            }
        }
    }

    "curry" - {
        val arg1 = 1
        val arg2 = 1.0
        val arg3 = 1.0F
        val arg4 = ""
        val arg5 = true
        val arg6 = 1L

        "a function of 2 arguments" {
            val curried: (Int) -> (Double) -> Int = curry(foo1)

            curried(arg1)(arg2) shouldBeExactly foo1(arg1, arg2)
        }

        "a function of 3 arguments" {
            val curried: (Int) -> (Double) -> (Float) -> Int = curry(foo2)

            curried(arg1)(arg2)(arg3) shouldBeExactly foo2(arg1, arg2, arg3)
        }

        "a function of 4 arguments" {
            val curried: (Int) -> (Double) -> (Float) -> (String) -> Int =
                curry(foo3)

            curried(arg1)(arg2)(arg3)(arg4) shouldBeExactly
                foo3(arg1, arg2, arg3, arg4)
        }

        "a function of 5 arguments" {
            val curried: (Int) ->
            (Double) ->
            (Float) ->
            (String) ->
            (Boolean) -> Int = curry(foo4)

            curried(arg1)(arg2)(arg3)(arg4)(arg5) shouldBeExactly
                foo4(arg1, arg2, arg3, arg4, arg5)
        }

        "a function of 6 arguments" {
            val curried: (Int) ->
            (Double) ->
            (Float) ->
            (String) ->
            (Boolean) ->
            (Long) -> Int = curry(foo5)

            curried(arg1)(arg2)(arg3)(arg4)(arg5)(arg6) shouldBeExactly
                foo5(arg1, arg2, arg3, arg4, arg5, arg6)
        }
    }

    "complement" - {
        "of a function of no arguments" {
            checkAll() { b: Boolean ->
                val f = { b }

                val complementF: () -> Boolean = complement(f)
                val r = complementF()

                r shouldBe !b
            }
        }

        "of a function of 1 argument" {
            checkAll() { b: Boolean ->
                val f = { _: Int -> b }

                val complementF: (Int) -> Boolean = complement(f)
                val r = complementF(0)

                r shouldBe !b
            }
        }

        "of a function of 2 arguments" {
            checkAll() { b: Boolean ->
                val f = { _: Int -> { _: Long -> b } }

                val complementF: (Int) -> (Long) -> Boolean = complement(f)
                val r = complementF(0)(0L)

                r shouldBe !b
            }
        }

        "of a function of 3 arguments" {
            checkAll() { b: Boolean ->
                val f = { _: Int -> { _: Long -> { _: String -> b } } }

                val complementF: (Int) ->
                (Long) ->
                (String) ->
                Boolean = complement(f)

                val r = complementF(0)(0L)("")

                r shouldBe !b
            }
        }

        "of a function of 4 arguments" {
            checkAll() { b: Boolean ->
                val f = { _: Int ->
                    { _: Long ->
                        { _: String ->
                            { _: Float ->
                                b
                            }
                        }
                    }
                }

                val complementF: (Int) ->
                (Long) ->
                (String) ->
                (Float) ->
                Boolean = complement(f)

                val r = complementF(0)(0L)("")(1.2F)

                r shouldBe !b
            }
        }
    }
})