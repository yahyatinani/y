package com.github.whyrising.y

interface Indexed<out E> : ConstantCount {
    fun nth(index: Int): E
}
