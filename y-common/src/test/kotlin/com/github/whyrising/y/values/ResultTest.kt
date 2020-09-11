package com.github.whyrising.y.values

import com.github.whyrising.y.values.Result.Failure
import com.github.whyrising.y.values.Result.Success
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.reflection.shouldBeData
import io.kotest.matchers.reflection.shouldBeSealed
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.io.Serializable

class ResultTest : FreeSpec({
    "Result should be sealed" {
        Result::class.shouldBeSealed()
    }

    "Result should be serializable" {
        Result::class.shouldBeSubtypeOf<Serializable>()
    }

    "Failure" - {
        "should be a subclass of Result" {
            Failure::class.shouldBeSubtypeOf<Result<*>>()
        }

        "should have an exception property of type RuntimeException" {
            val exception = RuntimeException("test error")

            val failure = Failure<Int>(exception)

            failure.exception shouldBe exception
        }

        "toString should return `Failure(exceptionMessage)`" {
            checkAll { message: String ->
                val failure = Failure<Int>(RuntimeException(message))

                failure.toString() shouldBe "Failure($message)"
            }
        }

        "should be a data class" { Failure::class.shouldBeData() }
    }

    "Success" - {
        "should be a subclass of Result" {
            Success::class.shouldBeSubtypeOf<Result<*>>()
        }

        "should have a property of a generic type T" {
            checkAll { i: Int, str: String ->
                val success1 = Success(i)
                val success2 = Success(str)

                success1.value shouldBe i
                success2.value shouldBe str
            }
        }

        "toString should return `Success(value)`" {
            checkAll { value: String ->
                val success = Success(value)

                success.toString() shouldBe "Success($value)"
            }
        }

        "Success type should be covariant" {
            checkAll { value: String ->
                val success = Success(value)

                success.toString() shouldBe "Success($value)"
            }
        }

        "should be a data class" { Success::class.shouldBeData() }
    }

    "invoke()" - {
        "when passed a null, it should return a Failure as Result" {
            val result = Result(null) as Failure

            shouldThrow<NullPointerException> { throw result.exception }
        }

        "when passed a value, it should return a Success as Result" {
            checkAll { i: Int ->
                val result = Result(i)

                result shouldBe Success(i)
            }
        }
    }

    "failure() companion function" - {
        "when passed a string message, it should return Failure as Result" {
            checkAll { str: String ->
                val result: Result<Int> = Result.failure(str)
                val failure: Failure<Int> = result as Failure<Int>

                shouldThrow<IllegalStateException> { throw failure.exception }
                failure.exception.message shouldBe str
            }
        }

        "when passed a RuntimeException, it should return the failure of it" {
            checkAll { str: String ->
                val result: Result<Int> = Result.failure(RuntimeException(str))
                val failure: Failure<Int> = result as Failure

                shouldThrow<RuntimeException> { throw failure.exception }
                failure.exception.message shouldBe str
            }
        }

        """
            when passed a Exception, it should wrap it in
            an IllegalStateException and return the failure of it
        """ {
            checkAll { str: String ->
                val result: Result<Int> = Result.failure(Exception(str))
                val failure: Failure<Int> = result as Failure

                shouldThrow<IllegalStateException> {
                    throw failure.exception
                }
                failure.exception.message shouldBe str
            }
        }
    }

    "Result should be generic and covariant" {
        val result: Result<Number> = Result<Int>()

        @Suppress("RemoveExplicitTypeArguments")
        val success: Success<Number> = Success<Int>(1)
        val failure: Failure<Number> = Failure<Int>(RuntimeException(""))

        success.value shouldBe 1
        failure.exception.message shouldBe ""
        shouldThrow<NullPointerException> {
            @Suppress("UNCHECKED_CAST")
            throw (result as Failure<Int>).exception
        }
    }
})
