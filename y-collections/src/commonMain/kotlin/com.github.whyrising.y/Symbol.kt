package com.github.whyrising.y

import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.util.getValue
import com.github.whyrising.y.util.hashCombine

class Symbol(override val name: String) : Named, IHashEq, Comparable<Symbol> {
    internal var hasheq: Int = 0

    override fun toString(): String = name

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Symbol -> false
        else -> name == other.name
    }

    override fun hashCode(): Int = hashCombine(name.hashCode(), 0)

    @ExperimentalStdlibApi
    override fun hasheq(): Int {
        if (hasheq == 0)
            hasheq = hashCombine(Murmur3.hashUnencodedChars(name), 0)

        return hasheq
    }

    override fun compareTo(other: Symbol): Int = when (other) {
        this -> 0
        else -> name.compareTo(other.name)
    }

    operator fun <K, V> invoke(map: Map<K, V>, default: V? = null): V? =
        getValue(this, map, default)
}

fun s(name: String): Symbol = Symbol(name)
