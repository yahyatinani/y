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
                notifyWatches(currentV, newVal)
                return newVal
            }
        }
    }

    override fun <A> swap(arg: A, f: (currentVal: T, arg: A) -> T): T {
        while (true) {
            val currentV = deref()
            val newVal = f(currentV, arg)
            validate(newVal)

            if (state.compareAndSet(currentV, newVal)) {
                notifyWatches(currentV, newVal)
                return newVal
            }
        }
    }

    override fun <A1, A2> swap(
        arg1: A1,
        arg2: A2,
        f: (currentVal: T, arg1: A1, arg2: A2) -> T
    ): T {
        while (true) {
            val currentV = deref()
            val newVal = f(currentV, arg1, arg2)
            validate(newVal)

            if (state.compareAndSet(currentV, newVal)) {
                notifyWatches(currentV, newVal)
                return newVal
            }
        }
    }

    override fun reset(newValue: T): T {
        validate(newValue)
        val oldValue = state.getAndSet(newValue)
        notifyWatches(oldValue, newValue)
        return newValue
    }

    fun compareAndSet(oldValue: T, newValue: T): Boolean {
        validate(newValue)
        val b = state.compareAndSet(oldValue, newValue)
        if (b)
            notifyWatches(oldValue, newValue)

        return b
    }
}
