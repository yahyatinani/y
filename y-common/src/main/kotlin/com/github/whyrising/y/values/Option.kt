package com.github.whyrising.y.values

sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    abstract fun <U> map(f: (T) -> U): Option<U>

    abstract fun <U> flatMap(f: (T) -> Option<U>): Option<U>

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> default()
    }

    fun orElse(default: () -> Option<@UnsafeVariance T>): Option<T> =
        map { this }.getOrElse(default)

    fun filter(p: (T) -> Boolean): Option<T> =
        flatMap { if (p(it)) this else None }

    internal object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun <U> map(f: (Nothing) -> U): Option<U> = None

        override fun <U> flatMap(f: (Nothing) -> Option<U>): Option<U> = None

        override fun toString(): String = "None"

        override fun hashCode(): Int = 0
    }

    internal data class Some<out T>(internal val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false

        private fun <U> some(f: (T) -> U) = Some(f(value))

        override fun <U> map(f: (T) -> U): Option<U> = some(f)

        override fun <U> flatMap(f: (T) -> Option<U>): Option<U> = some(f).value
    }

    companion object {
        operator fun <T> invoke(x: T? = null): Option<T> = when (x) {
            null -> None
            else -> Some(x)
        }
    }
}