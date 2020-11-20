package com.github.whyrising.y

interface Chunk<E> {
    fun dropFirst(): Chunk<E>
}