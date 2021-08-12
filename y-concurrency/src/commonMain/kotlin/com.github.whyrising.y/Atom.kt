package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

class Atom<T>(state: T) : IDeref<T> {
    internal val state: AtomicRef<T> = atomic(state)

    override fun deref(): T = state.value
}
