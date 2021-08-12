package com.github.whyrising.y

interface IAtom<T> {
    fun swap(f: (currentVal: T) -> T): T

    fun reset(newValue: T): T
    fun <A> swap(arg: A, f: (currentVal: T, arg: A) -> T): T
    fun <A1, A2> swap(
        arg1: A1,
        arg2: A2,
        f: (currentVal: T, arg1: A1, arg2: A2) -> T
    ): T
}
