package com.github.whyrising.y.collections.seq

import com.github.whyrising.y.collections.Chunk

interface IChunkedSeq<out E> {
    fun firstChunk(): Chunk<E>
    fun restChunks(): ISeq<E>
}
