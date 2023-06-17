package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.collections.PersistentList.Empty

class Cons<out E>(
  private val _first: E,
  private val _rest: ISeq<E>,
) : ASeq<E>() {

  override fun first(): E = _first

  override fun rest(): ISeq<E> = _rest

  override fun next(): ISeq<E>? = when (rest().seq()) {
    is Empty -> null
    else -> _rest
  }

  // Move to Util when needed
  private fun count(): Int {
    when (_rest) {
      is InstaCount -> return _rest.count
      else -> {
        var size = 0
        var rest = _rest
        while (rest !is Empty) {
          if (rest is InstaCount) return size + rest.count

          rest = rest.rest()
          size++
        }
        return size
      }
    }
  }

  override val count: Int
    get() = 1 + count()
}
