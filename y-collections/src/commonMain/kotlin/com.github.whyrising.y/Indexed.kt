package com.github.whyrising.y

interface Indexed<out E> : ConstantCount {
    fun nth(index: Int): E

    fun nth(index: Int, default: @UnsafeVariance E): E
}
