package com.github.whyrising.y.values

sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    abstract fun <U> map(f: (T) -> U): Option<U>

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> default()
    }

    internal object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun <U> map(f: (Nothing) -> U): Option<U> = None

        override fun toString(): String = "None"

        override fun hashCode(): Int = 0
    }

    internal data class Some<out T>(internal val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false

        override fun <U> map(f: (T) -> U): Option<U> = Some(f(value))
    }

    companion object {
        operator fun <T> invoke(x: T? = null): Option<T> = when (x) {
            null -> None
            else -> Some(x)
        }
    }
}