package com.github.whyrising.y

expect object RefFactory {
    fun <T : Any> create(any: T): Ref<T>
}