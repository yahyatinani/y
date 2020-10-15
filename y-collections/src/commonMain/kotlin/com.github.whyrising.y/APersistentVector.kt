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
}
