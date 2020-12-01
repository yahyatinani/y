package com.github.whyrising.y.stack

import com.github.whyrising.y.seq.IPersistentCollection

interface IPersistentStack<out E> : IPersistentCollection<E> {
    fun peek(): E?

    fun pop(): IPersistentStack<E>
}
