package com.github.whyrising.y

class ArrayChunk<E>(
    val array: Array<E>,
    val start: Int,
    val end: Int
) : Chunk<E> {

    constructor(array: Array<E>) : this(array, 0, array.size)

    override fun dropFirst(): Chunk<E> = when (start) {
        end -> throw IllegalStateException()
        else -> ArrayChunk(array, start + 1, end)
    }

    override val count: Int = end - start

    override fun nth(index: Int): E = array[start + index]

    override fun nth(index: Int, default: E): E = when (index) {
        in 0 until count -> nth(index)
        else -> default
    }
}
