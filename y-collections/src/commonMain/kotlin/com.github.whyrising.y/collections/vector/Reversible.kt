package com.github.whyrising.y.collections.vector

import com.github.whyrising.y.collections.seq.ISeq

interface Reversible<out E> {
    fun reverse(): ISeq<E>
}
