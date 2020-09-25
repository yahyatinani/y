package com.github.whyrising.y.values

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

/**
 * Is a monadic container type which represents the Empty, Failure or Success
 * values. It can be used when the data is optional or in case of failure.
 * @since 0.0.1
 */
@Serializable
sealed class Result<out T> {

    /**
     * @param f is the function expected to be applied to the value of
     * `this` Result.
     * @return It returns the `Result` of the result of applying f to `this`
     */
    abstract fun <R> map(f: (T) -> R): Result<R>

    abstract fun <R> flatMap(f: (T) -> Result<R>): Result<R>

    abstract fun getOrElse(defaultValue: @UnsafeVariance T): T

    abstract fun getOrElse(defaultValue: () -> @UnsafeVariance T): T

    abstract
    fun orElse(defaultValue: () -> Result<@UnsafeVariance T>): Result<T>

    abstract fun mapFailure(message: String): Result<T>

    abstract fun forEach(
        onSuccess: (T) -> Unit = {},
        onFailure: (RuntimeException) -> Unit = {},
        onEmpty: () -> Unit = {}
    )

    fun filter(message: String, predicate: (T) -> Boolean): Result<T> =
        flatMap {
            if (predicate(it)) this
            else failure(message)
        }

    fun filter(p: (T) -> Boolean): Result<T> =
        filter("Condition didn't hold", p)

    fun exists(p: (T) -> Boolean): Boolean = map(p).getOrElse(false)

    @Serializable
    internal abstract class None<T> : Result<T>() {
        override fun <R> map(f: (T) -> R): Result<R> = Empty

        override fun <R> flatMap(f: (T) -> Result<R>): Result<R> = Empty

        override fun getOrElse(defaultValue: T): T = defaultValue

        override fun getOrElse(defaultValue: () -> T): T = defaultValue()

        override fun orElse(defaultValue: () -> Result<T>): Result<T> = try {
            defaultValue()
        } catch (e: Exception) {
            handle(e)
        }

        override fun mapFailure(message: String): Result<T> = this

        override fun forEach(
            onSuccess: (T) -> Unit,
            onFailure: (RuntimeException) -> Unit,
            onEmpty: () -> Unit
        ) = onEmpty()

        override fun toString(): String = "Empty"
    }

    @Serializable
    internal object Empty : None<Nothing>()

    class FailureAsStringSerializer<T> : KSerializer<Failure<T>> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Color) {
            val string = value.rgb.toString(16).padStart(6, '0')
            encoder.encodeString(string)
        }

        override fun deserialize(decoder: Decoder): Color {
            val string = decoder.decodeString()
            return Color(string.toInt(16))
        }
    }

    @Serializable
    internal data class Failure<out T>(
        @Contextual
        internal val exception: RuntimeException
    ) : Result<T>() {

        override fun <R> map(f: (T) -> R): Result<R> = Failure(exception)

        override
        fun <R> flatMap(f: (T) -> Result<R>): Result<R> = Failure(exception)

        override
        fun getOrElse(defaultValue: @UnsafeVariance T): T = defaultValue

        override
        fun getOrElse(defaultValue: () -> @UnsafeVariance T): T = defaultValue()

        override fun orElse(
            defaultValue: () -> Result<@UnsafeVariance T>
        ): Result<T> = try {
            defaultValue()
        } catch (e: Exception) {
            handle(e)
        }

        override fun mapFailure(message: String): Result<T> =
            Failure(RuntimeException(message, exception))

        override fun forEach(
            onSuccess: (T) -> Unit,
            onFailure: (RuntimeException) -> Unit,
            onEmpty: () -> Unit
        ): Unit = onFailure(exception)

        override fun toString(): String = "Failure(${exception.message})"
    }

    @Serializable
    internal data class Success<out T>(internal val value: T) : Result<T>() {

        override fun <R> map(f: (T) -> R): Result<R> = try {
            Success(f(value))
        } catch (e: Exception) {
            handle(e)
        }

        override fun <R> flatMap(f: (T) -> Result<R>): Result<R> = try {
            f(value)
        } catch (e: Exception) {
            handle(e)
        }

        override fun getOrElse(defaultValue: @UnsafeVariance T): T = value

        override
        fun getOrElse(defaultValue: () -> @UnsafeVariance T): T = value

        override fun orElse(
            defaultValue: () -> Result<@UnsafeVariance T>
        ): Result<T> = this

        override fun mapFailure(message: String): Result<T> = this

        override fun forEach(
            onSuccess: (T) -> Unit,
            onFailure: (RuntimeException) -> Unit,
            onEmpty: () -> Unit
        ) = onSuccess(value)

        override fun toString(): String = "Success($value)"
    }

    companion object {
        operator fun <T> invoke(): Result<T> = Empty

        operator fun <T> invoke(t: T?): Result<T> = when (t) {
            null -> Failure(NullPointerException())
            else -> Success(t)
        }

        operator fun <T> invoke(t: T?, message: String): Result<T> = when (t) {
            null -> Failure(NullPointerException(message))
            else -> Success(t)
        }

        operator fun <T> invoke(t: T?, p: (T) -> Boolean): Result<T> =
            when (t) {
                null -> Failure(NullPointerException("t is null!"))
                else -> if (p(t)) Success(t) else Empty
            }

        operator
        fun <T> invoke(t: T?, message: String, p: (T) -> Boolean): Result<T> =
            when (t) {
                null -> Failure(NullPointerException(message))
                else -> when {
                    p(t) -> Success(t)
                    else -> failure("$t does not match condition: $message")
                }
            }

        fun <T> failure(message: String): Result<T> =
            Failure(IllegalStateException(message))

        fun <T> failure(exception: RuntimeException): Result<T> =
            Failure(exception)

        fun <T> failure(exception: Exception): Result<T> =
            Failure(IllegalStateException(exception))

        private fun <T> handle(e: Exception): Result<T> = when (e) {
            is RuntimeException -> failure(e)
            else -> failure(RuntimeException(e))
        }

        fun <T, R> lift(f: (T) -> R): (Result<T>) -> Result<R> = { it.map(f) }

        @JvmName("Result_lift1")
        fun <T1, T2, R> lift(
            f: (T1) -> (T2) -> R
        ): (Result<T1>) -> (Result<T2>) -> Result<R> = { r1: Result<T1> ->
            { r2: Result<T2> ->
                r1.map(f).flatMap { r2.map(it) }
            }
        }

        @JvmName("Result_lift2")
        fun <T1, T2, T3, R> lift(
            f: (T1) -> (T2) -> (T3) -> R
        ): (Result<T1>) -> (Result<T2>) -> (Result<T3>) -> Result<R> = { r1 ->
            { r2 ->
                { r3 ->
                    r1.map(f).flatMap { r2.map(it) }.flatMap { r3.map(it) }
                }
            }
        }

        fun <T1, T2, R> map(
            r1: Result<T1>,
            r2: Result<T2>,
            f: (T1) -> (T2) -> R
        ): Result<R> = r1.map(f).flatMap { r2.map(it) }

        fun <T1, T2, T3, R> map(
            r1: Result<T1>,
            r2: Result<T2>,
            r3: Result<T3>,
            f: (T1) -> (T2) -> (T3) -> R
        ): Result<R> = r1.map(f).flatMap { r2.map(it) }.flatMap { r3.map(it) }

        fun <T> of(f: () -> T): Result<T> = try {
            Result(f())
        } catch (e: RuntimeException) {
            failure(e)
        } catch (e: Exception) {
            failure(e)
        }

        fun <T> of(errMsg: String, f: () -> T): Result<T> {
            fun format(e: Exception, errMsg: String) =
                // TODO : check name vs simpleName
                "${e::class.simpleName}: [errMsg: $errMsg] " +
                    "[cause message: ${e.message}]"

            return try {
                Result(f())
            } catch (e: RuntimeException) {
                failure(format(e, errMsg))
            } catch (e: Exception) {
                failure(Exception(format(e, errMsg), e))
            }
        }

        fun <T> of(value: T, errMsg: String, p: (T) -> Boolean): Result<T> =
            try {
                if (p(value)) Success(value)
                else failure(
                    "Assertion failed for value $value with message: $errMsg"
                )
            } catch (e: Exception) {
                val message = "Exception while validating $value"
                failure(IllegalStateException(message, e))
            }
    }
}
