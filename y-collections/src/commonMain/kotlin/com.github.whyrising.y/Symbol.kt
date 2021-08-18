package com.github.whyrising.y

import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.map.IPersistentMap
import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.util.getValue
import com.github.whyrising.y.util.hashCombine

internal class Symbol(
    override val name: String
) : Named, IHashEq, Comparable<Symbol> {
    val str: String by lazy { name }

    @ExperimentalStdlibApi
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

    @ExperimentalStdlibApi
    override fun hasheq(): Int = hasheq

    override fun compareTo(other: Symbol): Int = when (other) {
        this -> 0
        else -> name.compareTo(other.name)
    }

    operator fun <K : Any, V : Any> invoke(
        map: IPersistentMap<K, V>,
        default: V? = null
    ): V? {
        return map.valAt(this as K, default)
    }

    operator fun <K : Any, V : Any> invoke(
        map: Map<K, V>,
        default: V? = null
    ): V? = getValue(map, this, default)
}

internal fun s(name: String): Symbol = Symbol(name)
