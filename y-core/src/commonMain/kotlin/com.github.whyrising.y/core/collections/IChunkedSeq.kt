package com.github.whyrising.y.core.collections

interface IChunkedSeq<out E> {
  fun firstChunk(): Chunk<E>
  fun restChunks(): ISeq<E>
}
