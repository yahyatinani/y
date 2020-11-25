package com.github.whyrising.y

import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.util.getValue

const val MAGIC = -0x61c88647

class Keyword internal constructor(
    val symbol: Symbol
) : Named, Comparable<Keyword>, IHashEq {
    internal var print: String = ""
        private set

    @ExperimentalStdlibApi
    val hasheq: Int = symbol.hasheq() + MAGIC

    override val name: String = symbol.name

    override fun toString(): String {
        if (print == "") print = ":${symbol.name}"
        return print
    }

    @ExperimentalStdlibApi
    override fun hasheq(): Int = hasheq

    override fun hashCode(): Int = symbol.hashCode() + MAGIC

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Keyword -> false
        else -> this.symbol == other.symbol
    }

    override fun compareTo(other: Keyword): Int = symbol.compareTo(other.symbol)
    
    operator fun <V> invoke(map: Map<Keyword, V>, default: V? = null): V? =
        getValue(this, map, default)
}
