package com.github.whyrising.y

interface Reversible<out E> {
    fun reverse(): ISeq<E>
}
