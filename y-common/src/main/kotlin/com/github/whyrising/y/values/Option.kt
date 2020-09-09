package com.github.whyrising.y.values

sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> default()
    }

    internal object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun toString(): String = "None"

        override fun hashCode(): Int = 0
    }

    internal data class Some<out T>(internal val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false
    }

    companion object {
        operator fun <T> invoke(x: T? = null): Option<T> = when (x) {
            null -> None
            else -> Some(x)
        }
    }
}