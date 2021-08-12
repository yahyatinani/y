package com.github.whyrising.y

interface IAtom<T> {
    fun swap(f: (currentVal: T) -> T): T

    fun reset(newValue: T): T
}
