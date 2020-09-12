package com.github.whyrising.y.values

import com.github.whyrising.y.values.Result.Failure
import com.github.whyrising.y.values.Result.Success
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.reflection.shouldBeData
import io.kotest.matchers.reflection.shouldBeSealed
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import java.io.Serializable

const val EXCEPTION_MESSAGE = "java.lang.Exception: "

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
            checkAll { i: Int, msg: String ->
                val success1 = Success(i)
                val success2 = Success(msg)

                success1.value shouldBe i
                success2.value shouldBe msg
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
            checkAll { msg: String ->
                val result: Result<Int> = Result.failure(msg)
                val failure: Failure<Int> = result as Failure<Int>

                shouldThrow<IllegalStateException> { throw failure.exception }
                failure.exception.message shouldBe msg
            }
        }

        "when passed a RuntimeException, it should wrap it in a failure" {
            checkAll { msg: String ->
                val result: Result<Int> = Result.failure(RuntimeException(msg))
                val failure: Failure<Int> = result as Failure

                shouldThrowExactly<RuntimeException> { throw failure.exception }
                failure.exception.message shouldBe msg
            }
        }

        """
            when passed a Exception, it should wrap it in
            an IllegalStateException and wrap it again in a failure
        """ {
            checkAll { msg: String ->
                val result: Result<Int> = Result.failure(Exception(msg))
                val failure: Failure<Int> = result as Failure

                shouldThrow<IllegalStateException> {
                    throw failure.exception
                }
                failure.exception.message shouldBe "$EXCEPTION_MESSAGE$msg"
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

    "map()" - {
        val f = { i: Int -> i.toDouble() }

        "when called on Failure, it should return a failure" {
            val failure = Result.failure<Int>("test")

            val r: Result<Double> = failure.map(f)

            r shouldBe failure
        }

        "when called on Success" - {
            "it should return the mapped value in a Success" {
                checkAll { i: Int ->
                    val success = Result(i)

                    val r: Result<Double> = success.map(f)

                    r shouldBe Result(f(i))
                }
            }



            "when the mapping function throws an exception" - {
                val success = Result(1)

                "map() shouldn't throw" {
                    checkAll { msg: String ->
                        val g: (Int) -> Double = { throw RuntimeException(msg) }
                        val h: (Int) -> Double = { throw Exception(msg) }

                        shouldNotThrow<RuntimeException> { success.map(g) }
                        shouldNotThrow<Exception> { success.map(h) }
                    }
                }

                "map() should wrap it and return a failure" {
                    checkAll { msg: String ->
                        val g: (Int) -> Double = { throw RuntimeException(msg) }
                        val h: (Int) -> Double = { throw Exception(msg) }

                        val r1 = success.map(g) as Failure<Double>
                        val r2 = success.map(h) as Failure<Double>

                        val e1 = r1.exception
                        val e2 = r2.exception
                        shouldThrowExactly<RuntimeException> { throw e1 }
                        shouldThrowExactly<RuntimeException> { throw e2 }
                        e1.message shouldBe msg
                        e2.message shouldBe "$EXCEPTION_MESSAGE$msg"
                    }
                }
            }
        }
    }

    "flatMap()" - {
        val f: (Int) -> Result<Double> = { i: Int -> Result(i.toDouble()) }

        "when called on Failure, it should return a failure" {
            val failure = Result.failure<Int>("test")

            val r: Result<Double> = failure.flatMap(f)

            r shouldBe failure
        }

        "when called on Success" - {
            "it should return the mapped value in a Success" {
                checkAll { i: Int ->
                    val success = Result(i)

                    val r: Result<Double> = success.flatMap(f)

                    r shouldBe f(i)
                }
            }

            "when the mapping function throws an exception" - {
                val success = Result(1)
                "flatMap() shouldn't throw" {
                    checkAll { msg: String ->
                        val g: (Int) -> Result<Double> = {
                            throw RuntimeException(msg)
                        }
                        val h: (Int) -> Result<Double> = {
                            throw Exception(msg)
                        }

                        shouldNotThrow<RuntimeException> { success.flatMap(g) }
                        shouldNotThrow<Exception> { success.flatMap(h) }
                    }
                }

                "flatMap() should wrap it and return a failure" {
                    checkAll { msg: String ->
                        val g: (Int) -> Result<Double> = {
                            throw RuntimeException(msg)
                        }
                        val h: (Int) -> Result<Double> = {
                            throw Exception(msg)
                        }

                        val r1 = success.flatMap(g) as Failure<Double>
                        val r2 = success.flatMap(h) as Failure<Double>
                        val e1 = r1.exception
                        val e2 = r2.exception

                        shouldThrowExactly<RuntimeException> { throw e1 }
                        shouldThrowExactly<RuntimeException> { throw e2 }
                        e1.message shouldBe msg
                        e2.message shouldBe "$EXCEPTION_MESSAGE$msg"
                    }
                }
            }
        }
    }
})
