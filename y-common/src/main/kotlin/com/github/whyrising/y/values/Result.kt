package com.github.whyrising.y.values

import java.io.Serializable
import java.lang.IllegalStateException

sealed class Result<out T> : Serializable {

    abstract fun <R> map(f: (T) -> R): Result<R>

    internal data class Failure<out T>(
        internal val exception: RuntimeException
    ) : Result<T>() {

        override fun <R> map(f: (T) -> R): Result<R> = Failure(exception)

        override fun toString(): String = "Failure(${exception.message})"
    }

    internal data class Success<out T>(internal val value: T) : Result<T>() {

        override fun <R> map(f: (T) -> R): Result<R> = try {
            Success(f(value))
        } catch (e: RuntimeException) {
            failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun toString(): String = "Success($value)"
    }

    companion object {
        operator
        fun <T> invoke(t: T? = null): Result<T> = when (t) {
            null -> Failure(NullPointerException())
            else -> Success(t)
        }

        fun <T> failure(message: String): Result<T> =
            Failure(IllegalStateException(message))

        fun <T> failure(exception: RuntimeException): Result<T> =
            Failure(exception)

        fun <T> failure(exception: Exception): Result<T> =
            Failure(IllegalStateException(exception))
    }
}
