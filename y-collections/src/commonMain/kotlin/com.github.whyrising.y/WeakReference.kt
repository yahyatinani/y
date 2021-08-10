package com.github.whyrising.y

expect class WeakReference<T : Any>(ref: T) {
    val value: T?

    fun clear()
}
