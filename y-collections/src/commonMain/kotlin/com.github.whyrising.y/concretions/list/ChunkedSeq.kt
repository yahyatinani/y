package com.github.whyrising.y.concretions.list

import com.github.whyrising.y.ArrayChunk
import com.github.whyrising.y.Chunk
import com.github.whyrising.y.concretions.list.PersistentList.Empty
import com.github.whyrising.y.seq.IChunkedSeq
import com.github.whyrising.y.seq.ISeq

class ChunkedSeq<out E>(
    private val firstChunk: Chunk<E>,
    internal val restChunks: ISeq<E>
) : ASeq<E>(), IChunkedSeq<E> {

    constructor(firstChunk: ArrayChunk<E>) : this(firstChunk, Empty)

    override fun first(): E = firstChunk.nth(0)

    override fun rest(): ISeq<E> = when {
        firstChunk.count > 1 -> ChunkedSeq(firstChunk.dropFirst(), restChunks)
        else -> restChunks()
    }

    override fun firstChunk(): Chunk<E> = firstChunk

    override fun restChunks(): ISeq<E> = restChunks
}
