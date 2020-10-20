package com.github.whyrising.y

interface Seqable<out E> {
    fun seq(): ISeq<E>?
}
