package com.github.whyrising.y

import com.github.whyrising.y.core.IHashEq
import com.github.whyrising.y.map.IPersistentMap
import com.github.whyrising.y.util.getValue
import com.github.whyrising.y.utils.clearCache
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

object KeywordSerializer : KSerializer<Keyword> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Keyword", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Keyword) =
        encoder.encodeString(value.name)

    override fun deserialize(decoder: Decoder): Keyword =
        k(decoder.decodeString())
}

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
        map: IPersistentMap<K, V>,
        default: V? = null
    ): V? {
        return map.valAt(this as K, default)
    }

    operator fun <K : Any, V : Any> invoke(
        map: Map<K, V>,
        default: V? = null
    ): V? {
        return getValue(map, this, default)
    }

    companion object {
        const val MAGIC = -0x61c88647

        internal val cache: ConcurrentHashMap<Symbol, Reference<Keyword>> =
            ConcurrentHashMap<Symbol, Reference<Keyword>>()

        private val rq = ReferenceQueue<Keyword>()

        internal operator fun invoke(sym: Symbol): Keyword {
            var previousRef: Reference<Keyword>? = cache[sym]

            if (previousRef == null) {
                clearCache(rq, cache)
                val keyword = Keyword(sym)
                previousRef = cache.putIfAbsent(sym, WeakReference(keyword, rq))

                if (previousRef == null)
                    return keyword
            }

            val previousKey: Keyword? = previousRef.get()

            if (previousKey != null)
                return previousKey

            // if key got garbage collected, remove from cache, do over
            cache.remove(sym, previousRef)
            return invoke(sym)
        }

        internal operator fun invoke(name: String) = invoke(Symbol(name))
    }
}

fun k(name: String): Keyword = Keyword.invoke(name)
