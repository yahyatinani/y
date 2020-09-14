package com.github.whyrising.y.values

import com.github.whyrising.y.idOdd
import com.github.whyrising.y.isEven
import com.github.whyrising.y.values.Result.Failure
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.io.IOException

class AssertionsTest : FreeSpec({
    "assertCondition(value: T, failMsg: String, p: (T) -> Boolean)" - {
        "when p throws, it should return a Failure" {
            val value = 111
            val cause = IOException()
            val p: (Int) -> Boolean = { throw cause }

            val failure = assertCondition(value, "", p) as Failure<Int>
            val exception = failure.exception

            exception.message shouldBe "Exception while validating $value"
            shouldThrowExactly<IllegalStateException> { throw exception }
            exception.cause shouldBe cause
        }

        "when p() returns" - {
            "when condition holds, it should return Result of passed value" {
                checkAll(Arb.int().filter(isEven)) { i: Int ->
                    val result = assertCondition(i, "", isEven)

                    result shouldBe Result(i)
                }
            }

            "when condition fails, it should return Failure of passed msg" {
                checkAll(
                    Arb.int().filter(idOdd),
                    Arb.string()
                ) { i: Int, msg: String ->

                    val r = assertCondition(i, msg, isEven) as Failure<Int>

                    r.exception.message shouldBe
                        "Assertion failed for value $i with message: $msg"
                }
            }
        }
    }

    "assertCondition(value: T, p: (T) -> Boolean)" - {
        "when condition fails, it should return Failure with default message" {
            checkAll(Arb.int().filter(idOdd)) { i: Int ->
                val default = "condition should be true"
                val expectedErr = "Assertion failed for value $i " +
                    "with message: $default"

                val r = assertCondition(i, isEven) as Failure<Int>

                r.exception.message shouldBe expectedErr
            }
        }
    }

    "assertTrue()" - {
        "when condition holds, it should return Result of true" {

            val result: Result<Boolean> = assertTrue(true)

            result shouldBe Result(true)
        }

        "when condition fails, it should return a Failure with error message" {
            val default = "condition should be true"
            val prefix = "Assertion failed for value false with message:"

            checkAll { str: String ->
                val r1 = assertTrue(false) as Failure<Boolean>
                val r2 = assertTrue(false, str) as Failure<Boolean>

                r1.exception.message shouldBe "$prefix $default"
                r2.exception.message shouldBe "$prefix $str"
            }
        }
    }

    "assertFalse()" - {
        "when condition holds, it should return Result of false" {

            val result: Result<Boolean> = assertFalse(false)

            result shouldBe Result(false)
        }

        "when condition fails, it should return a Failure with error message" {
            val default = "condition should be false"
            val prefix = "Assertion failed for value true with message:"

            checkAll { str: String ->
                val r1 = assertFalse(true) as Failure<Boolean>
                val r2 = assertFalse(true, str) as Failure<Boolean>

                r1.exception.message shouldBe "$prefix $default"
                r2.exception.message shouldBe "$prefix $str"
            }
        }
    }

    "assertNotNull(t: T, failMsg: String)" - {
        "when condition holds, it should return Result of t" {
            checkAll { i: Int ->
                val result: Result<Int?> = assertNotNull(i, "")

                result shouldBe Result(i)
            }
        }

        "when condition fails, it should return a Failure with a message" {
            checkAll { msg: String ->
                val prefix = "Assertion failed for value null with message:"

                val r = assertNotNull(null, msg) as Failure<Int?>

                r.exception.message shouldBe "$prefix $msg"
            }
        }
    }

    "assertNotNull(t: T)" - {
        "when condition fails, it should return Failure with a message" {
            val prefix = "Assertion failed for value null with message:"
            val default = "object should not be null"

            val r = assertNotNull(null) as Failure<Int?>

            r.exception.message shouldBe "$prefix $default"
        }
    }

    "assertPositive(n:Int, msg:String)" - {
        "when condition holds, it should return the Result of the n" {
            checkAll(Arb.int().filter { it > 0 }) { positive: Int ->
                val r = assertPositive(positive)

                r shouldBe Result(positive)
            }
        }

        "when condition fails, it should return the Result of the n" {
            val genNegatives = Arb.int().filter { it <= 0 }

            checkAll(genNegatives, Arb.string()) { n: Int, msg: String ->
                val prefix = "Assertion failed for value $n with message:"
                val default = "number should be n"

                val r1 = assertPositive(n) as Failure<Int>
                val r2 = assertPositive(n, msg) as Failure<Int>

                r1.exception.message shouldBe "$prefix $default"
                r2.exception.message shouldBe "$prefix $msg"
            }
        }
    }
})
