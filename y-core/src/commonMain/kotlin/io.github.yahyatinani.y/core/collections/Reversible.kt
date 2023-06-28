package io.github.yahyatinani.y.core.collections

interface Reversible<out E> {
  fun reverse(): ISeq<E>
}
