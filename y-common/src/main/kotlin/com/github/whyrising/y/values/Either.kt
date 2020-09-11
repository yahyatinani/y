package com.github.whyrising.y.values

sealed class Either<out L, out R> {

    abstract fun <T> map(f: (R) -> T): Either<L, T>

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("mapLeft")
    abstract fun <T> map(f: (L) -> T): Either<T, R>

    abstract
    fun <T> flatMap(f: (R) -> Either<@UnsafeVariance L, T>): Either<L, T>

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("flatMapLeft")
    abstract
    fun <T> flatMap(f: (L) -> Either<T, @UnsafeVariance R>): Either<T, R>

    internal
    data class Left<out L, out R>(internal val value: L) : Either<L, R>() {

        override fun <T> map(f: (R) -> T): Either<L, T> = Left(value)

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("mapLeft")
        override fun <T> map(f: (L) -> T): Either<T, R> = Left(f(value))

        override
        fun <T> flatMap(f: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> =
            Left(value)

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("flatMapLeft")
        override
        fun <T> flatMap(f: (L) -> Either<T, @UnsafeVariance R>): Either<T, R> =
            f(value)

        override fun toString(): String = "Left($value)"
    }

    internal
    data class Right<out L, out R>(internal val value: R) : Either<L, R>() {

        override fun <T> map(f: (R) -> T): Either<L, T> = Right(f(value))

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("mapLeft")
        override fun <T> map(f: (L) -> T): Either<T, R> = Right(value)

        override
        fun <T> flatMap(f: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> =
            f(value)

        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("flatMapLeft")
        override
        fun <T> flatMap(f: (L) -> Either<T, @UnsafeVariance R>): Either<T, R> =
            Right(value)

        override fun toString(): String = "Right($value)"
    }

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Left(value)

        fun <L, R> right(value: R): Either<L, R> = Right(value)
    }
}