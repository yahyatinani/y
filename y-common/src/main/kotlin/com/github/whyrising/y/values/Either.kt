package com.github.whyrising.y.values

sealed class Either<out L, out R> {
    internal class Left<out L, out R>(internal val value: L) : Either<L, R>() {
        override fun toString(): String = "Left($value)"
    }

    internal class Right<out L, out R>(internal val value: R) : Either<L, R>() {
        override fun toString(): String = "Right($value)"
    }

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)

        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }
}