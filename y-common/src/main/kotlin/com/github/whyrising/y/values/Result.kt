package com.github.whyrising.y.values

import java.io.Serializable
import java.lang.IllegalStateException

sealed class Result<out T> : Serializable {
    internal
    data class Failure<out T>(
        internal val exception: RuntimeException
    ) : Result<T>() {

        override fun toString(): String = "Failure(${exception.message})"
    }

    internal data class Success<out T>(internal val value: T) : Result<T>() {

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
            Failure(RuntimeException(exception.message))

        fun <T> failure(exception: Exception): Result<T> =
            Failure(IllegalStateException(exception.message))
    }
}
