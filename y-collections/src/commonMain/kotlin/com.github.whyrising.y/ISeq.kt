package com.github.whyrising.y

interface ISeq<out E> {
    fun first(): E

    fun rest(): ISeq<E>

    fun cons(e: @UnsafeVariance E): ISeq<E>
}
