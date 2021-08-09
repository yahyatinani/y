package com.github.whyrising.y

import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.util.getValue
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object KeywordSerializer : KSerializer<Keyword> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Keyword", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Keyword) =
        encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): Keyword =
        k(decoder.decodeString())
}

internal val keywordsCache = hashMapOf<Symbol, Any>()

@Serializable(KeywordSerializer::class)
class Keyword private constructor(
    internal val symbol: Symbol
) : Named, Comparable<Keyword>, IHashEq {
    val str: String by lazy { ":${symbol.name}" }

    @Transient
    @ExperimentalStdlibApi
    internal val hashEq: Int = symbol.hasheq() + MAGIC

    override val name: String = symbol.name

    override fun toString(): String = str

    @ExperimentalStdlibApi
    override fun hasheq(): Int = hashEq

    override fun hashCode(): Int = symbol.hashCode() + MAGIC

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Keyword -> false
        else -> this.symbol == other.symbol
    }

    override fun compareTo(other: Keyword): Int = symbol.compareTo(other.symbol)

    operator fun <K : Any, V : Any> invoke(
        map: Map<K, V>,
        default: V? = null
    ): V? = getValue(this, map, default)

    companion object {
        const val MAGIC = -0x61c88647
        private val lock = reentrantLock()

        internal operator fun invoke(sym: Symbol): Keyword {
            var existingRef = keywordsCache[sym]

            if (existingRef == null) {
                val keyword = Keyword(sym)

                lock.withLock {
                    existingRef =
                        keywordsCache.put(sym, RefFactory.create(keyword))
                }

                if (existingRef == null) return keyword
            }

            val existingKey = RefFactory.valueOf<Keyword>(existingRef!!)

            if (existingKey != null) return existingKey

            lock.withLock {
                // if key got garbage collected, remove from cache
                keywordsCache.remove(sym)
            }

            return invoke(sym)
        }

        internal operator fun invoke(name: String) = invoke(Symbol(name))
    }
}

fun k(name: String): Keyword = Keyword.invoke(name)
