package com.github.whyrising.y.values

sealed class Either<out L, out R> {

    abstract fun <T> map(f: (R) -> T): Either<L, T>

    internal
    data class Left<out L, out R>(internal val value: L) : Either<L, R>() {

        override fun <T> map(f: (R) -> T): Either<L, T> = Left(value)

        override fun toString(): String = "Left($value)"
    }

    internal
    data class Right<out L, out R>(internal val value: R) : Either<L, R>() {

        override fun <T> map(f: (R) -> T): Either<L, T> = Right(f(value))

        override fun toString(): String = "Right($value)"
    }

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)

        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }
}