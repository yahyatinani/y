package com.github.whyrising.y.collections.mutable.collection

interface IMutableCollection<out E> {
    fun asTransient(): ITransientCollection<E>
}
