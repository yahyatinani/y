package com.github.whyrising.y.values

import java.io.Serializable

sealed class Result<out T> : Serializable {

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

    internal object Empty : None<Nothing>()

    internal data class Failure<out T>(
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
        ) = onFailure(exception)

        override fun toString(): String = "Failure(${exception.message})"
    }

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
    }
}
