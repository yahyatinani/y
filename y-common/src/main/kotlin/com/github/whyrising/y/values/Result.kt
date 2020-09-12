package com.github.whyrising.y.values

import java.io.Serializable

sealed class Result<out T> : Serializable {

    abstract fun <R> map(f: (T) -> R): Result<R>

    abstract fun <R> flatMap(f: (T) -> Result<R>): Result<R>

    abstract fun getOrElse(defaultValue: @UnsafeVariance T): T

    abstract
    fun orElse(defaultValue: () -> Result<@UnsafeVariance T>): Result<T>

    internal data class Failure<out T>(
        internal val exception: RuntimeException
    ) : Result<T>() {

        override fun <R> map(f: (T) -> R): Result<R> = Failure(exception)

        override
        fun <R> flatMap(f: (T) -> Result<R>): Result<R> = Failure(exception)

        override
        fun getOrElse(defaultValue: @UnsafeVariance T): T = defaultValue

        override fun orElse(
            defaultValue: () -> Result<@UnsafeVariance T>
        ): Result<T> = try {
            defaultValue()
        } catch (e: Exception) {
            handle(e)
        }

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

        override fun orElse(
            defaultValue: () -> Result<@UnsafeVariance T>
        ): Result<T> = this

        override fun toString(): String = "Success($value)"
    }

    companion object {
        operator fun <T> invoke(t: T? = null): Result<T> = when (t) {
            null -> Failure(NullPointerException())
            else -> Success(t)
        }

        fun <T> failure(message: String): Result<T> =
            Failure(IllegalStateException(message))

        fun <T> failure(exception: RuntimeException): Result<T> =
            Failure(exception)

        fun <T> failure(exception: Exception): Result<T> =
            Failure(IllegalStateException(exception))

        private fun <T> handle(e: Exception): Result<T> = when (e) {
            is RuntimeException -> Failure(e)
            else -> Failure(RuntimeException(e))
        }
    }
}
