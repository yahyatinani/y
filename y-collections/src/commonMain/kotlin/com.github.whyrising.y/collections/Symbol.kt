package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.core.IHashEq
import com.github.whyrising.y.collections.core.getFrom
import com.github.whyrising.y.collections.util.Murmur3
import com.github.whyrising.y.collections.util.hashCombine

internal class Symbol(
    override val name: String
) : Named, IHashEq, Comparable<Symbol> {
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
        default: V? = null
    ): V? = getFrom(map, this, default)
}

internal fun s(name: String): Symbol = Symbol(name)
