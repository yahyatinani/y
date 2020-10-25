package com.github.whyrising.y

interface IPersistentStack<out E> {
    fun peek(): E?

    fun pop(): IPersistentStack<E>
}
