package com.github.whyrising.y

interface IAtom2<T> : IAtom<T> {
    fun swapVals(f: (currentVal: T) -> T): Pair<T, T>

    fun <A> swapVals(arg: A, f: (currentVal: T, arg: A) -> T): Pair<T, T>

    fun <A1, A2> swapVals(
        arg1: A1,
        arg2: A2,
        f: (currentVal: T, arg1: A1, arg2: A2) -> T
    ): Pair<T, T>

    fun resetVals(newValue: T): Pair<T, T>
}