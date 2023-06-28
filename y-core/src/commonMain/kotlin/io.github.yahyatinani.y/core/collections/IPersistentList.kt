package io.github.yahyatinani.y.core.collections

interface IPersistentList<out E> :
  IPersistentCollection<E>,
  IPersistentStack<E>,
  Sequential
