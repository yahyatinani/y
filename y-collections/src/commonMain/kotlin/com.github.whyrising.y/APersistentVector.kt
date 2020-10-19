package com.github.whyrising.y

abstract class APersistentVector<out E> : IPersistentVector<E> {
    override fun toString(): String {
        var i = 0
        var str = ""
        while (i < count) {
            str += "${nth(i)} "
            i++
        }

        return "[${str.trim()}]"
    }

    override fun length(): Int = count

    protected fun indexOutOfBounds(index: Int) = index >= count || index < 0

    override fun nth(index: Int, default: @UnsafeVariance E): E = when {
        indexOutOfBounds(index) -> default
        else -> nth(index)
    }
}
