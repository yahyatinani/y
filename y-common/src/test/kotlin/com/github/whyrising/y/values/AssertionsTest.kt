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
                val default = "Assertion error: condition should be true"
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
            val default = "Assertion error: condition should be true"
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
            val default = "Assertion error: condition should be false"
            val prefix = "Assertion failed for value true with message:"

            checkAll { str: String ->
                val r1 = assertFalse(true) as Failure<Boolean>
                val r2 = assertFalse(true, str) as Failure<Boolean>

                r1.exception.message shouldBe "$prefix $default"
                r2.exception.message shouldBe "$prefix $str"
            }
        }
    }
})
