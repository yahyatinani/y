package com.github.whyrising.y.collection

interface IMutableCollection<out E> {
    fun asTransient(): ITransientCollection<E>
}
