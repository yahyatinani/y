package com.github.whyrising.y.values

import com.github.whyrising.y.core.complement
import com.github.whyrising.y.core.str
import com.github.whyrising.y.values.Result.Companion.lift
import com.github.whyrising.y.values.Result.Companion.map
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
import java.io.IOException
import java.io.Serializable

const val EXCEPTION_MESSAGE = "java.lang.Exception: "

@Suppress("UNCHECKED_CAST")
class ResultTest : FreeSpec({
    val isEven: (Int) -> Boolean = { it % 2 == 0 }
    val idOdd = complement(isEven)

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

    "invoke() without any arguments, it should return Empty as Result" {
        Result<Int>() shouldBe Empty
    }

    "invoke(T?)" - {
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

    "invoke(T?, String)" - {
        "when T is null, return Failure with the message passed" {
            checkAll { message: String ->
                val result: Result<Int> = Result(null, message)

                val exception = (result as Failure<Int>).exception

                exception.message shouldBe message
                shouldThrowExactly<NullPointerException> { throw exception }
            }
        }

        "when T is valid, return a Success of T" {
            checkAll { i: Int, message: String ->
                val result: Result<Int> = Result(i, message)

                result shouldBe Success(i)
            }
        }
    }

    "invoke(T?, predicate)" - {
        "when T is null, return Failure" {
            val result: Result<Int> = Result(null, isEven)

            val exception = (result as Failure<Int>).exception

            exception.message shouldBe "t is null!"
            shouldThrowExactly<NullPointerException> { throw exception }
        }

        "when T is valid" - {
            "when the condition holds, it should return Success of T" {
                checkAll(Arb.int().filter(isEven)) { i: Int ->
                    val result: Result<Int> = Result(i, isEven)

                    result shouldBe Success(i)
                }
            }

            "when condition fails, it should return Empty" {
                checkAll(Arb.int().filter(idOdd)) { i: Int ->
                    val result: Result<Int> = Result(i, isEven)

                    result shouldBe Empty
                }
            }
        }
    }

    "invoke(T?, String, predicate)" - {
        "when T is null, return Failure with the message passed" {
            checkAll { message: String ->
                val result: Result<Int> = Result(null, message, isEven)

                val exception = (result as Failure<Int>).exception

                exception.message shouldBe message
                shouldThrowExactly<NullPointerException> { throw exception }
            }
        }

        "when T is valid" - {
            "when the condition holds, it should return Success of T" {
                checkAll(
                    Arb.int().filter(isEven),
                    Arb.string()
                ) { i: Int, message: String ->
                    val result: Result<Int> = Result(i, message, isEven)

                    result shouldBe Success(i)
                }
            }

            "when condition fails, it should return a Failure with message " {
                checkAll(
                    Arb.int().filter(idOdd),
                    Arb.string()
                ) { i: Int, message: String ->
                    val result: Result<Int> = Result(i, message, isEven)

                    val exception = (result as Failure<Int>).exception

                    exception.message shouldBe
                        "$i does not match condition: $message"
                    shouldThrowExactly<IllegalStateException> {
                        throw exception
                    }
                }
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

                    val r = evenNumber.filter(idOdd) as Failure<Int>
                    val e = r.exception

                    e.message shouldBe "Condition didn't hold"
                    shouldThrowExactly<IllegalStateException> { throw e }
                }
            }
        }

        "when called on a Failure, it should return it" {
            checkAll { message: String ->
                val failure = Result.failure<Int>(message)

                val r = failure.filter(idOdd)

                r shouldBe failure
            }
        }

        "when called on Empty, it should return Empty" {
            val empty: Result<Int> = Empty

            val r = empty.filter(idOdd)

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

                    val r = evenNumber.filter(msg, idOdd) as Failure<Int>
                    val e = r.exception

                    e.message shouldBe msg
                    shouldThrowExactly<IllegalStateException> { throw e }
                }
            }
        }

        "when called on a Failure, it should return it" {
            checkAll { message: String ->
                val failure = Result.failure<Int>(message)

                val r = failure.filter("Doesn't match", idOdd) as Failure<Int>
                val e = r.exception

                e.message shouldBe message
                shouldThrowExactly<IllegalStateException> { throw e }
            }
        }

        "when called on Empty, it should return Empty" {
            checkAll { message: String ->
                val empty: Result<Int> = Empty

                val r = empty.filter(message, idOdd)

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
                val evenNumber = Result(i)
                val empty = Result<Int>()
                val failure = Result<Int>(null)

                val b1: Boolean = evenNumber.exists(idOdd)
                val b2: Boolean = empty.exists(idOdd)
                val b3: Boolean = failure.exists(idOdd)

                b1.shouldBeFalse()
                b2.shouldBeFalse()
                b3.shouldBeFalse()
            }
        }
    }

    "mapFailure(message:String)" - {
        "when called on Success or Empty, it should return `this`" {
            checkAll { message: String ->
                val success: Result<Int> = Result(10)
                val empty: Result<Int> = Empty

                val r1: Result<Int> = success.mapFailure(message)
                val r2: Result<Int> = empty.mapFailure(message)

                r1 shouldBe success
                r2 shouldBe empty
            }
        }

        """
            when called on a Failure,
            it should wrap it with the new message and return a new Failure
        """ {
            checkAll { oldMessage: String, newMessage: String ->
                val failure: Result<Int> = Result.failure(oldMessage)

                val r = failure.mapFailure(newMessage) as Failure<Int>
                val exception = r.exception

                exception.message shouldBe newMessage
                exception.cause shouldBe (failure as Failure<Int>).exception
                shouldThrowExactly<RuntimeException> { throw exception }
            }
        }
    }

    "forEach(onSuccess(), onFailure(), onEmpty)" - {
        val default = 0

        "when called with default effects, it should do nothing" {
            val failure = Result.failure<Int>("test")
            val empty = Result<Int>()
            val success = Result(default)

            failure.forEach()
            empty.forEach()
            success.forEach()
        }

        "when called on a Failure, it should apply onFailure() only" {
            var i = default
            var j = default
            var k = default
            val onSuccess: (Int) -> Unit = { i++ }
            val onFailure: (RuntimeException) -> Unit = { j++ }
            val onEmpty: () -> Unit = { k++ }

            val failure = Result.failure<Int>("test")

            failure.forEach(onSuccess, onFailure, onEmpty)

            i shouldBe default
            j shouldBe default + 1
            k shouldBe default
        }

        "when called on a Empty, it should apply onEmpty() only" {
            var i = default
            var j = default
            var k = default
            val onSuccess: (Int) -> Unit = { i++ }
            val onFailure: (RuntimeException) -> Unit = { j++ }
            val onEmpty: () -> Unit = { k++ }
            val empty = Result<Int>()

            empty.forEach(onSuccess, onFailure, onEmpty)

            i shouldBe default
            j shouldBe default
            k shouldBe default + 1
        }

        "when called on a Success, it should apply onSuccess() only" {
            var i = default
            var j = default
            var k = default
            val onSuccess: (Int) -> Unit = { i++ }
            val onFailure: (RuntimeException) -> Unit = { j++ }
            val onEmpty: () -> Unit = { k++ }
            val success = Result(default)

            success.forEach(onSuccess, onFailure, onEmpty)

            i shouldBe default + 1
            j shouldBe default
            k shouldBe default
        }
    }

    "lift(f:(T)->R) should transform f to Result(A) -> Result(B)" {
        checkAll { n: Int, l: Long ->
            val f1: (Int) -> String = { i: Int -> str(i) }
            val f2: (Long) -> String = { i: Long -> str(i) }

            val g1: (Result<Int>) -> Result<String> = lift(f1)
            val g2: (Result<Long>) -> Result<String> = lift(f2)

            g1(Result(n)) shouldBe Result(f1(n))
            g2(Result(l)) shouldBe Result(f2(l))
        }
    }

    "lift(f: T1->T2->R) should transform f to Result(A)->Result(B)->Result(C)" {
        checkAll { n: Int, l: Long, f: Float ->
            val f1: (Int) -> (Float) -> String = { { f -> str(it, f) } }
            val f2: (Long) -> (Float) -> String = { { f -> str(it, f) } }

            val g1: (Result<Int>) ->
            (Result<Float>) ->
            Result<String> = lift(f1)

            val g2: (Result<Long>) ->
            (Result<Float>) ->
            Result<String> = lift(f2)

            g1(Result(n))(Result(f)) shouldBe Result(f1(n)(f))
            g2(Result(l))(Result(f)) shouldBe Result(f2(l)(f))
        }
    }

    """
        lift(f: T1->T2->T3->R) should transform f to 
        Result(A) -> Result(B) -> Result(C) -> Result(D)
    """ {
        checkAll { n: Int, x: Float, y: Double ->
            val f: (Int) ->
            (Float) ->
            (Double) ->
            String = { { f -> { d -> str(it, f, d) } } }

            val g: (Result<Int>) ->
            (Result<Float>) ->
            (Result<Double>) ->
            Result<String> = lift(f)

            g(Result(n))(Result(x))(Result(y)) shouldBe Result(f(n)(x)(y))
        }
    }

    "map(Result(T1), Result(T2), f:(T1)->(T2)->R) should return Result(R)" {
        checkAll { n: Int, l: Long, d: Double ->
            val result1 = Result(n)
            val result2 = Result(l)
            val result3 = Result(d)
            val f1: (Int) -> (Long) -> String = { { m -> str(it, m) } }
            val f2: (Int) -> (Double) -> String = { { m -> str(it, m) } }

            val r1: Result<String> = map(result1, result2, f1)
            val r2: Result<String> = map(result1, result3, f2)

            r1 shouldBe Result(f1(n)(l))
            r2 shouldBe Result(f2(n)(d))
        }
    }

    """
        map(Result(A), Result(B), Result(C), f:(A)->(B)->(C)->D)
        should return Result(D)
    """ {
        checkAll { n: Int, l: Long, d: Double ->
            val r1 = Result(n)
            val r2 = Result(l)
            val r3 = Result(d)
            val f: (Int) ->
            (Long) ->
            (Double) ->
            String = { { m -> { k -> str(it, m, k) } } }

            val r: Result<String> = map(r1, r2, r3, f)

            r shouldBe Result(f(n)(l)(d))
        }
    }

    "Result.of(f: () -> T)" - {
        "when f throws, `of` should return a failure of that exception" {
            val f: () -> Int = { throw IllegalStateException() }
            val g: () -> Double = { throw IOException() }

            val exception1 = (Result.of(f) as Failure<Int>).exception
            val exception2 = (Result.of(g) as Failure<Double>).exception

            shouldThrowExactly<IllegalStateException> { throw exception1 }
            shouldThrowExactly<IllegalStateException> { throw exception2 }
            exception2.cause shouldBe IOException()
        }

        "when f returns, `of` should return Result of `f`" {

            checkAll { i: Int ->
                val f: () -> Int = { i }

                val result: Result<Int> = Result.of(f)

                result.forEach(onSuccess = { it shouldBe i })
            }
        }
    }
})
