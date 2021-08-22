package com.github.whyrising.y.collections.stack

import com.github.whyrising.y.collections.seq.IPersistentCollection

interface IPersistentStack<out E> : IPersistentCollection<E> {
    fun peek(): E?

    fun pop(): IPersistentStack<E>
}
