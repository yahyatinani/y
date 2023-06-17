package com.github.whyrising.y.core

import com.github.whyrising.y.core.collections.IHashEq
import com.github.whyrising.y.core.collections.Named
import com.github.whyrising.y.core.util.Murmur3
import com.github.whyrising.y.core.util.hashCombine

class Symbol(override val name: String) :
  Named,
  IHashEq,
  Comparable<Symbol> {
  val str: String by lazy { name }

  internal val hasheq: Int by lazy {
    hashCombine(Murmur3.hashUnencodedChars(name), 0)
  }

  override fun toString(): String = str

  override fun equals(other: Any?): Boolean = when {
    this === other -> true
    other !is Symbol -> false
    else -> name == other.name
  }

  override fun hashCode(): Int = hashCombine(name.hashCode(), 0)

  override fun hasheq(): Int = hasheq

  override fun compareTo(other: Symbol): Int = when (other) {
    this -> 0
    else -> name.compareTo(other.name)
  }

  operator fun <V> invoke(
    map: Any,
    default: V? = null,
  ): V? = get(map, this, default)
}
