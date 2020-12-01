package com.github.whyrising.y

import com.github.whyrising.y.vector.Indexed

interface Chunk<out E> : Indexed<E> {
    fun dropFirst(): Chunk<E>
}
