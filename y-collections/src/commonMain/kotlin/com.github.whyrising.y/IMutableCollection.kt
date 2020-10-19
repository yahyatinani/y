package com.github.whyrising.y

interface IMutableCollection<out E> {
    fun asTransient(): ITransientCollection<E>
}
