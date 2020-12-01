package com.github.whyrising.y.seq

import com.github.whyrising.y.Chunk

interface IChunkedSeq<out E> {
    fun firstChunk(): Chunk<E>
    fun restChunks(): ISeq<E>
}
