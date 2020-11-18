package com.github.whyrising.y.stack

interface IPersistentStack<out E> {
    fun peek(): E?

    fun pop(): IPersistentStack<E>
}
