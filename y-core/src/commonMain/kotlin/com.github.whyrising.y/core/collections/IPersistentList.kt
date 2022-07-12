package com.github.whyrising.y.core.collections

interface IPersistentList<out E> :
  IPersistentCollection<E>,
  IPersistentStack<E>,
  Sequential
