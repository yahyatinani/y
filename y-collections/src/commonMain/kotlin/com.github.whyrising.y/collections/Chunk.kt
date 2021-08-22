package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.vector.Indexed

interface Chunk<out E> : Indexed<E> {
    fun dropFirst(): Chunk<E>
}
