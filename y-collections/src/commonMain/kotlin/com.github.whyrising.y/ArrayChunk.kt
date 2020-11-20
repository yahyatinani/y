package com.github.whyrising.y

class ArrayChunk<E>(
    val array: Array<E>,
    val start: Int,
    val end: Int
) : Chunk<E> {

    override fun dropFirst(): Chunk<E> = when (start) {
        end -> throw IllegalStateException()
        else -> ArrayChunk(array, start + 1, end)
    }
}
