package com.github.whyrising.y.vector

import com.github.whyrising.y.core.ConstantCount

interface Indexed<out E> : ConstantCount {
    fun nth(index: Int): E

    fun nth(index: Int, default: @UnsafeVariance E): E
}
