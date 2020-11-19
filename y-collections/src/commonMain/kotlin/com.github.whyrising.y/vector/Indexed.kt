package com.github.whyrising.y.vector

import com.github.whyrising.y.core.InstaCount

interface Indexed<out E> : InstaCount {
    fun nth(index: Int): E

    fun nth(index: Int, default: @UnsafeVariance E): E
}
