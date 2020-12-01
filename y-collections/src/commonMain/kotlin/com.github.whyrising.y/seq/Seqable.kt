package com.github.whyrising.y.seq

interface Seqable<out E> {
    fun seq(): ISeq<E>
}
