package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.collections.PersistentList.Empty

class ChunkedSeq<out E>(
  private val firstChunk: Chunk<E>,
  internal val restChunks: ISeq<E>,
) : ASeq<E>(), IChunkedSeq<E> {

  constructor(firstChunk: ArrayChunk<E>) : this(firstChunk, Empty)

  override fun first(): E = firstChunk.nth(0)

  override fun rest(): ISeq<E> = when {
    firstChunk.count > 1 -> ChunkedSeq(firstChunk.dropFirst(), restChunks)
    else -> restChunks()
  }

  override fun next(): ISeq<E>? = when {
    firstChunk.count > 1 -> ChunkedSeq(firstChunk.dropFirst(), restChunks)
    else -> when (restChunks.seq()) {
      is Empty -> null
      else -> restChunks
    }
  }

  override fun firstChunk(): Chunk<E> = firstChunk

  override fun restChunks(): ISeq<E> = restChunks
}
