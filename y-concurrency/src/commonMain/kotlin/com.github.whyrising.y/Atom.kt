package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

class Atom<T>(state: T) : ARef<T>(), IAtom<T> {
    internal val state: AtomicRef<T> = atomic(state)

    override fun deref(): T = state.value

    override fun swap(f: (currentVal: T) -> T): T {
        while (true) {
            val currentV = deref()
            val newVal = f(currentV)
            validate(newVal)

            if (state.compareAndSet(currentV, newVal)) {
                // TODO: notify
                return newVal
            }
        }
    }
}
