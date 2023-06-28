package io.github.yahyatinani.y.core.collections

interface Seqable<out E> {
  fun seq(): ISeq<E>
}
