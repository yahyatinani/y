package com.github.whyrising.y.collections.seq

interface Seqable<out E> {
    fun seq(): ISeq<E>
}
