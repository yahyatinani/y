package com.github.whyrising.y

import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.util.getValue
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

const val MAGIC = -0x61c88647

private val lock = reentrantLock()
private val cache = hashMapOf<Symbol, Any>()

internal fun keywordsCache(): Map<Symbol, Any> = cache

class Keyword private constructor(
    internal val symbol: Symbol
) : Named, Comparable<Keyword>, IHashEq {

    internal var print: String = ""
        private set

    @ExperimentalStdlibApi
    internal val hashEq: Int = symbol.hasheq() + MAGIC

    override val name: String = symbol.name

    override fun toString(): String {
        if (print == "") print = ":${symbol.name}"
        return print
    }

    @ExperimentalStdlibApi
    override fun hasheq(): Int = hashEq

    override fun hashCode(): Int = symbol.hashCode() + MAGIC

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Keyword -> false
        else -> this.symbol == other.symbol
    }

    override fun compareTo(other: Keyword): Int = symbol.compareTo(other.symbol)

    operator fun <V> invoke(map: Map<Keyword, V>, default: V? = null): V? =
        getValue(this, map, default)

    companion object {
        operator fun invoke(sym: Symbol): Keyword {
            var existingRef = cache[sym]

            if (existingRef == null) {
                val keyword = Keyword(sym)

                lock.withLock {
                    existingRef = cache.put(sym, RefFactory.create(keyword))
                }

                if (existingRef == null) return keyword
            }

            val existingKey = RefFactory.valueOf<Keyword>(existingRef!!)

            if (existingKey != null) return existingKey

            lock.withLock {
                // if key got garbage collected, remove from cache
                cache.remove(sym)
            }

            return invoke(sym)
        }

        operator fun invoke(name: String): Keyword = invoke(Symbol(name))
    }
}
