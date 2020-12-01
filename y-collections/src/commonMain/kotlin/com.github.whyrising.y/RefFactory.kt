package com.github.whyrising.y

expect object RefFactory {
    fun <T : Any> create(any: T): Any

    fun <T : Any> valueOf(ref: Any): T?
}
