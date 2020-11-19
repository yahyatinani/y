package com.github.whyrising.y.mutable.collection

interface IMutableCollection<out E> {
    fun asTransient(): ITransientCollection<E>
}
