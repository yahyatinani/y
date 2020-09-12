package com.github.whyrising.y.values

import com.github.whyrising.y.values.Result.Empty
import com.github.whyrising.y.values.Result.Failure
import com.github.whyrising.y.values.Result.None
import com.github.whyrising.y.values.Result.Success
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.reflection.shouldBeData
import io.kotest.matchers.reflection.shouldBeSealed
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.io.Serializable

const val EXCEPTION_MESSAGE = "java.lang.Exception: "

@Suppress("UNCHECKED_CAST")
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

    "Empty" - {
        "should be a subclass of Result" {
            None::class.shouldBeSubtypeOf<Result<*>>()
        }

        "Empty be an object" {
            Empty
        }

        "Empty should be a subclass of None<Nothing>" {
            Empty::class.shouldBeSubtypeOf<None<Nothing>>()
        }

        "toString() should return `Empty`" {
            Empty.toString() shouldBe "Empty"
        }
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

        "without any arguments, it should return Empty as Result" {
            Result<Int>() shouldBe Empty
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

        @Suppress("RemoveExplicitTypeArguments", "UNUSED_VARIABLE")
        val success: Success<Number> = Success<Int>(1)

        @Suppress("RemoveExplicitTypeArguments", "UNUSED_VARIABLE")
        val result: Result<Number> = Result<Int>(12)

        @Suppress("UNUSED_VARIABLE")
        val failure: Failure<Number> = Failure<Int>(RuntimeException(""))
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

        "when called on Empty, it should return Empty" {
            Empty.map(f) shouldBe Empty
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
                val faulty1: (String) -> (Int) -> Result<Double> = { msg ->
                    { throw RuntimeException(msg) }
                }
                val faulty2: (String) -> (Int) -> Result<Double> = { msg ->
                    { throw Exception(msg) }
                }

                "flatMap() shouldn't throw" {
                    checkAll { msg: String ->
                        val g = faulty1(msg)
                        val h = faulty2(msg)

                        shouldNotThrow<RuntimeException> { success.flatMap(g) }
                        shouldNotThrow<Exception> { success.flatMap(h) }
                    }
                }

                "flatMap() should wrap it and return a failure" {
                    checkAll { msg: String ->
                        val g = faulty1(msg)
                        val h = faulty2(msg)

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

        "when called on Empty, it should return Empty" {
            Empty.flatMap(f) shouldBe Empty
        }
    }

    "getOrElse()" - {
        val default = -1

        "when called on Failure, it should return the default value" {
            val result = Result.failure<Int>("test")

            val r = result.getOrElse(default)

            r shouldBe default
        }

        "when called on Success, it should return the actual value" {
            checkAll(Arb.int().filter { it != default }) { i: Int ->
                val result = Result(i)

                val r = result.getOrElse(default)

                r shouldNotBe default
                r shouldBe i
            }
        }

        "when called on Empty, it should return the default value" {
            val result: Result<Int> = Empty

            result.getOrElse(default) shouldBe default
        }
    }

    "getOrEle() taking a constant function" - {
        val default: () -> Int = { -1 }

        "when called on Failure, it should return the default value" {
            val result = Result.failure<Int>("test")

            val r = result.getOrElse(default)

            r shouldBe default()
        }

        "when called on Success, it should return the actual value" {
            checkAll(Arb.int().filter { it != default() }) { i: Int ->
                val result = Result(i)

                val r = result.getOrElse(default)

                r shouldNotBe default()
                r shouldBe i
            }
        }

        "when called on Empty, it should return the default value" {
            val result: Result<Int> = Empty

            result.getOrElse(default) shouldBe default()
        }
    }

    "orElse()" - {
        val default: () -> Result<Int> = { Result(-1) }

        "when called on Success, it should return the actual value" {
            checkAll { i: Int ->
                val result = Result(i)

                val r = result.orElse(default)

                r shouldBe result
            }
        }

        "when called on Failure" - {
            val failure = Result.failure<Int>("test")

            "it should return the default value" {

                val r = failure.orElse(default)

                r shouldBe default()
            }

            "when the default function throws an exception" - {

                "orElse() shouldn't throw" {
                    val faulty1 = { throw RuntimeException() }
                    val faulty2 = { throw Exception() }

                    shouldNotThrow<RuntimeException> { failure.orElse(faulty1) }
                    shouldNotThrow<RuntimeException> { failure.orElse(faulty2) }
                }

                "orElse() should wrap it and return a failure" {

                    checkAll { msg: String ->
                        val faulty1 = { throw RuntimeException(msg) }
                        val faulty2 = { throw Exception(msg) }

                        val r1 = failure.orElse(faulty1) as Failure<Double>
                        val r2 = failure.orElse(faulty2) as Failure<Double>

                        val e1 = r1.exception
                        val e2 = r2.exception

                        e1.message shouldBe msg
                        e2.message shouldBe "$EXCEPTION_MESSAGE$msg"
                        shouldThrowExactly<RuntimeException> { throw e1 }
                        shouldThrowExactly<RuntimeException> { throw e2 }
                    }
                }
            }
        }

        "when called on Empty" - {
            val empty: Result<Int> = Empty

            "it should return the default value" {

                val r = empty.orElse(default)

                r shouldBe default()
            }

            "when the default function throws an exception" - {

                "orElse() shouldn't throw" {
                    val faulty1 = { throw RuntimeException() }
                    val faulty2 = { throw Exception() }

                    shouldNotThrow<RuntimeException> { empty.orElse(faulty1) }
                    shouldNotThrow<RuntimeException> { empty.orElse(faulty2) }
                }

                "orElse() should wrap it and return a failure" {

                    checkAll { msg: String ->
                        val faulty1 = { throw RuntimeException(msg) }
                        val faulty2 = { throw Exception(msg) }

                        val r1 = empty.orElse(faulty1) as Failure<Double>
                        val r2 = empty.orElse(faulty2) as Failure<Double>

                        val e1 = r1.exception
                        val e2 = r2.exception

                        e1.message shouldBe msg
                        e2.message shouldBe "$EXCEPTION_MESSAGE$msg"
                        shouldThrowExactly<RuntimeException> { throw e1 }
                        shouldThrowExactly<RuntimeException> { throw e2 }
                    }
                }
            }
        }
    }

    val isEven = { n: Int -> n % 2 == 0 }
    "filter(predicate: (T) -> Boolean)" - {

        "when called on a Success" - {
            "when the condition holds, it should return Success" {
                checkAll(Arb.int().filter(isEven)) { i: Int ->
                    val evenNumber = Result(i)

                    val r: Result<Int> = evenNumber.filter(isEven)

                    r shouldBe evenNumber
                }
            }

            "when the condition fails, it should return a Failure" {
                checkAll(Arb.int().filter(isEven)) { i: Int ->
                    val evenNumber = Result(i)
                    val isOdd = { n: Int -> n % 2 != 0 }

                    val r = evenNumber.filter(isOdd) as Failure<Int>
                    val e = r.exception

                    e.message shouldBe "Condition didn't hold"
                    shouldThrowExactly<IllegalStateException> { throw e }
                }
            }
        }

        "when called on a Failure, it should return it" {
            checkAll { message: String ->
                val failure = Result.failure<Int>(message)

                val r = failure.filter { n: Int -> n % 2 != 0 }

                r shouldBe failure
            }
        }

        "when called on Empty, it should return Empty" {
            val empty: Result<Int> = Empty

            val r = empty.filter { n: Int -> n % 2 != 0 }

            r shouldBe Empty
        }
    }

    "filter(message:String, predicate: (T) -> Boolean)" - {

        "when called on a Success" - {
            "when the condition holds, it should return Success" {
                checkAll(Arb.int().filter(isEven), Arb.string()) { i, msg ->
                    val evenNumber = Result(i)

                    val r: Result<Int> = evenNumber.filter(msg, isEven)

                    r shouldBe evenNumber
                }
            }

            "when the condition fails, it should return a Failure" {
                checkAll(Arb.int().filter(isEven), Arb.string()) { i, msg ->
                    val evenNumber = Result(i)
                    val isOdd = { n: Int -> n % 2 != 0 }

                    val r = evenNumber.filter(msg, isOdd) as Failure<Int>
                    val e = r.exception

                    e.message shouldBe msg
                    shouldThrowExactly<IllegalStateException> { throw e }
                }
            }
        }

        "when called on a Failure, it should return it" {
            checkAll { message: String ->
                val failure = Result.failure<Int>(message)
                val isOdd = { n: Int -> n % 2 != 0 }

                val r = failure.filter("Doesn't match", isOdd) as Failure<Int>
                val e = r.exception

                e.message shouldBe message
                shouldThrowExactly<IllegalStateException> { throw e }
            }
        }

        "when called on Empty, it should return Empty" {
            checkAll { message: String ->
                val empty: Result<Int> = Empty
                val isOdd = { n: Int -> n % 2 != 0 }

                val r = empty.filter(message, isOdd)

                r shouldBe Empty
            }
        }
    }

    "exits(p: (T) -> Boolean)" - {
        "when the condition holds, it should return true" {
            checkAll(Arb.int().filter(isEven)) { i: Int ->
                val evenNumber = Result(i)

                val b: Boolean = evenNumber.exists(isEven)

                b.shouldBeTrue()
            }
        }

        "when the condition fails, it should return false" {
            checkAll(Arb.int().filter(isEven)) { i: Int ->
                val isOdd = { n: Int -> n % 2 != 0 }
                val evenNumber = Result(i)
                val empty = Result<Int>()
                val failure = Result<Int>(null)

                val b1: Boolean = evenNumber.exists(isOdd)
                val b2: Boolean = empty.exists(isOdd)
                val b3: Boolean = failure.exists(isOdd)

                b1.shouldBeFalse()
                b2.shouldBeFalse()
                b3.shouldBeFalse()
            }
        }
    }
})
