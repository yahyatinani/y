package com.github.whyrising.y.vector

import com.github.whyrising.y.seq.ISeq

interface Reversible<out E> {
    fun reverse(): ISeq<E>
}
