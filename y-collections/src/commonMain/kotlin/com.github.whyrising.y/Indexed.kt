package com.github.whyrising.y

interface Indexed<out E> {
    fun nth(index: Int): E
}
