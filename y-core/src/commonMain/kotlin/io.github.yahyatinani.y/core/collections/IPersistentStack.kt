package io.github.yahyatinani.y.core.collections

interface IPersistentStack<out E> : IPersistentCollection<E> {
  fun peek(): E?

  fun pop(): IPersistentStack<E>
}
