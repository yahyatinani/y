package io.github.yahyatinani.y.core.collections

interface Chunk<out E> : Indexed<E> {
  fun dropFirst(): Chunk<E>
}
