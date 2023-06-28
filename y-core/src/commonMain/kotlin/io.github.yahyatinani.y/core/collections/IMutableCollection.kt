package io.github.yahyatinani.y.core.collections

interface IMutableCollection<out E> {
  fun asTransient(): ITransientCollection<E>
}
