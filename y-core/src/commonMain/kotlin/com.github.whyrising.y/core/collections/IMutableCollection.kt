package com.github.whyrising.y.core.collections

interface IMutableCollection<out E> {
  fun asTransient(): ITransientCollection<E>
}
