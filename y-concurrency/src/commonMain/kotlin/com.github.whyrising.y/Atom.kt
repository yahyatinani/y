package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

class Atom<T>(x: T) : ARef<T>(), IAtom2<T> {
    private val _state: AtomicRef<T> = atomic(x)

    internal val state by _state

    override fun deref(): T = state

    override fun swap(f: (currentVal: T) -> T): T {
        while (true) {
            val currentV = deref()
            val newVal = f(currentV)
            validate(newVal)

            if (_state.compareAndSet(currentV, newVal)) {
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

            if (_state.compareAndSet(currentV, newVal)) {
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

            if (_state.compareAndSet(currentV, newVal)) {
                notifyWatches(currentV, newVal)
                return newVal
            }
        }
    }

    override fun swapVals(f: (currentVal: T) -> T): Pair<T, T> {
        while (true) {
            val oldValue = deref()
            val newVal = f(oldValue)
            validate(newVal)

            if (_state.compareAndSet(oldValue, newVal)) {
                notifyWatches(oldValue, newVal)
                return oldValue to newVal
            }
        }
    }

    override fun <A> swapVals(
        arg: A,
        f: (currentVal: T, arg: A) -> T
    ): Pair<T, T> {
        while (true) {
            val oldValue = deref()
            val newVal = f(oldValue, arg)
            validate(newVal)

            if (_state.compareAndSet(oldValue, newVal)) {
                notifyWatches(oldValue, newVal)
                return oldValue to newVal
            }
        }
    }

    override fun <A1, A2> swapVals(
        arg1: A1,
        arg2: A2,
        f: (currentVal: T, arg1: A1, arg2: A2) -> T
    ): Pair<T, T> {
        while (true) {
            val oldValue = deref()
            val newVal = f(oldValue, arg1, arg2)
            validate(newVal)

            if (_state.compareAndSet(oldValue, newVal)) {
                notifyWatches(oldValue, newVal)
                return oldValue to newVal
            }
        }
    }

    override fun reset(newValue: T): T {
        validate(newValue)
        val oldValue = _state.getAndSet(newValue)
        notifyWatches(oldValue, newValue)
        return newValue
    }

    fun compareAndSet(oldValue: T, newValue: T): Boolean {
        validate(newValue)
        val b = _state.compareAndSet(oldValue, newValue)
        if (b)
            notifyWatches(oldValue, newValue)

        return b
    }

    override fun resetVals(newValue: T): Pair<T, T> {
        validate(newValue)
        val oldValue = _state.getAndSet(newValue)
        notifyWatches(oldValue, newValue)
        return oldValue to newValue
    }

    /** @return the value of the atom by calling deref() */
    operator fun invoke(): T = deref()
}

/**
 * @param x an initial value to be hold by the atom.
 * @return an `Atom<T>` with an initial value x.
 */
fun <T> atom(x: T): Atom<T> = Atom(x)
