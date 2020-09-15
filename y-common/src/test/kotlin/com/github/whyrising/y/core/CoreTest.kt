package com.github.whyrising.y.core

import com.github.whyrising.y.`string?`
import com.github.whyrising.y.foo1
import com.github.whyrising.y.foo2
import com.github.whyrising.y.foo3
import com.github.whyrising.y.foo4
import com.github.whyrising.y.foo5
import com.github.whyrising.y.values.Result
import com.github.whyrising.y.values.Result.Failure
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
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
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll
import java.lang.IllegalStateException
import java.math.BigInteger
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

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
        "when passing no arguments, It should return the empty string." {
            val r = str()

            r shouldBe ""
        }

        "when passing 1 arg, it returns the string value of the arg" {
            checkAll(Arb.`string?`()) { a: String? ->
                val expected: String = when (a) {
                    null -> ""
                    else -> a.toString()
                }

                val r = str(a)

                r shouldBe expected
            }
        }

        "when passing 2 args, it returns the string concatenation of the two" {
            checkAll(Arb.`string?`(), Arb.`string?`())
            { a: String?, b: String? ->
                val expected: String = when (a) {
                    null -> ""
                    else -> a.toString()
                }.let {
                    when (b) {
                        null -> it
                        else -> "$it$b"
                    }
                }

                val r = str(a, b)

                r shouldBe expected
            }
        }

        "when passing 3 args, it returns the string concatenation of the args" {
            checkAll(
                Arb.`string?`(),
                Arb.`string?`(),
                Arb.`string?`()
            ) { a: String?, b: String?, c: String? ->
                val expected: String = when (a) {
                    null -> ""
                    else -> a.toString()
                }.let {
                    when (b) {
                        null -> it
                        else -> "$it$b"
                    }
                }.let {
                    when (c) {
                        null -> it
                        else -> "$it$c"
                    }
                }

                val r = str(a, b, c)

                r shouldBe expected
            }
        }

        "when passing vararg it returns the string concatenation of all args" {
            checkAll(
                Arb.`string?`(),
                Arb.`string?`(),
                Arb.`string?`(),
                Arb.list(Arb.`string?`())
            ) { a: String?, b: String?, c: String?, list: List<String?> ->
                val expected: String = when (a) {
                    null -> ""
                    else -> a.toString()
                }.let {
                    when (b) {
                        null -> it
                        else -> "$it$b"
                    }
                }.let {
                    when (c) {
                        null -> it
                        else -> "$it$c"
                    }
                }.let {
                    list.fold(it) { acc, s ->
                        when (s) {
                            null -> acc
                            else -> "$acc$s"
                        }
                    }
                }

                val r = str(a, b, c, *list.toTypedArray())

                r shouldBe expected
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
            checkAll { b: Boolean ->
                val f = { b }

                val complementF: () -> Boolean = complement(f)
                val r = complementF()

                r shouldBe !b
            }
        }

        "of a function of 1 argument" {
            checkAll { b: Boolean ->
                val f = { _: Int -> b }

                val complementF: (Int) -> Boolean = complement(f)
                val r = complementF(0)

                r shouldBe !b
            }
        }

        "of a function of 2 arguments" {
            checkAll { b: Boolean ->
                val f = { _: Int -> { _: Long -> b } }

                val complementF: (Int) -> (Long) -> Boolean = complement(f)
                val r = complementF(0)(0L)

                r shouldBe !b
            }
        }

        "of a function of 3 arguments" {
            checkAll { b: Boolean ->
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
            checkAll { b: Boolean ->
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

    "compose" - {
        "with no arguments passed, should return the identity function" {
            checkAll { i: Int ->
                val comp = compose<Int>()

                val r = comp(i)

                r shouldBeExactly i
            }
        }

        "one function, should return the same function" {
            val f1: (Int) -> Int = ::inc
            val g1: (Float) -> Float = ::inc

            val f2: (Int) -> Int = compose(f1)
            val g2: (Float) -> Float = compose(g1)

            f2 shouldBe f1
            g2 shouldBe g1
        }

        "two functions `f` and `g`" - {
            "when g has no args, should return the composition with no args " {
                checkAll { n: Int ->
                    val f: (Int) -> String = { i: Int -> str(i) }
                    val g: () -> Int = { n }

                    val fog: () -> String = compose(f, g)

                    fog() shouldBe f(g())
                }
            }

            "when g has 1 arg, should return the composition with 1 arg" {
                checkAll { n: Int, x: Float ->
                    val f: (Int) -> String = { i: Int -> str(i) }
                    val g: (Float) -> Int = { n }

                    val fog: (Float) -> String = compose(f, g)

                    fog(x) shouldBe f(g(x))
                }
            }

            "when g has 2 args, should return the composition with 2 args" {
                checkAll { n: Int, x: Float, y: Double ->
                    val f: (Int) -> String = { i: Int -> str(i) }
                    val g: (Float) -> (Double) -> Int = { { n } }

                    val fog: (Float) -> (Double) -> String = compose(f, g)

                    fog(x)(y) shouldBe f(g(x)(y))
                }
            }

            "when g has 3 args, should return the composition with 3 args" {
                checkAll { n: Int, x: Float, y: Double, z: Boolean ->
                    val f: (Int) -> String = { i: Int -> str(i) }
                    val g: (Float) -> (Double) -> (Boolean) -> Int =
                        { { { n } } }

                    val fog: (Float) -> (Double) -> (Boolean) -> String =
                        compose(f, g)

                    fog(x)(y)(z) shouldBe f(g(x)(y)(z))
                }
            }
        }
    }

    "retry(times: Int, delay: Long = 10, f: (A) -> B)" - {
        "when f throws, it should retry x times " - {
            "when f fails every time, retry should return a Failure" {
                var i = 0
                var j = 0
                val times = 3
                val e1 = IllegalStateException("failed")
                val e2 = Exception("failed")
                val f: (Int) -> Double = {
                    i += 1
                    throw e1
                }
                val g: (Int) -> Double = {
                    j += 1
                    throw e2
                }

                val r1 = retry(times, f)(i) as Failure<Double>
                val r2 = retry(times, g)(j) as Failure<Double>

                i shouldBeExactly times
                r1.exception shouldBe e1
                j shouldBeExactly times
                r2.exception shouldBe IllegalStateException(e2)
            }

            "when f returns after few failures, retry should return a Success" {
                var i = 0
                val times = 3
                val e = IllegalStateException("failed")
                val f: (Int) -> Double = {
                    i += 1
                    if (i < 3) throw e
                    else i.toDouble()
                }

                val r: Result<Double> = retry(times, f)(i)

                r shouldBe Result(i.toDouble())
            }

            "delay should be 10 by default" {
                var delay = 0L
                val f: (Int) -> Double = { throw IllegalStateException() }
                val sleepFun: (Long) -> Unit = { delay = it }

                retry(3, f, sleep = sleepFun)(1)

                delay shouldBeExactly 10L
            }

            "should sleep for the given delay" {
                val f: (Int) -> Double = { throw IllegalStateException() }
                val delay: Long = 50

                measureTimeMillis {
                    retry(2, f, delay = delay)(1)
                } shouldBeGreaterThanOrEqual delay
            }
        }

        "when f doesn't fail, retry should return a Success" {
            val times = 3
            checkAll { i: Int ->
                val f: (Int) -> Double = { it.toDouble() }

                val r: Result<Double> = retry(times, f)(i)

                r shouldBe Result(i.toDouble())
            }
        }
    }
})

fun <T, R> retry(
    times: Int,
    f: (T) -> R,
    delay: Long = 10L,
    sleep: (Long) -> Unit = Thread::sleep,
): (T) -> Result<R> {
    fun retry(t: T, result: Result<R>, e: Result<R>, count: Int): Result<R> =
        result.orElse {
            when (count) {
                0 -> e
                else -> {
                    sleep(delay)
                    retry(t, Result.of { f(t) }, result, count - 1)
                }
            }
        }

    return { t: T -> retry(t, Result.of { f(t) }, Result(), times - 1) }
}
