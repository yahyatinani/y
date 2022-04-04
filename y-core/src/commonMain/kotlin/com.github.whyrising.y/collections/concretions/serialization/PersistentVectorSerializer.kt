package com.github.whyrising.y.collections.concretions.serialization

import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection

internal const val PERSISTENT_VECTOR_NAME =
    "com.github.whyrising.y.collections.concretions.vector.PersistentVector"

class PersistentVectorSerializer<E>(private val eSerializer: KSerializer<E>) :
    KSerializer<PersistentVector<E>> {

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor(
        PERSISTENT_VECTOR_NAME,
        ListSerializer(eSerializer).descriptor
    )

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): PersistentVector<E> {
        var ret = PersistentVector<E>()
        val compositeDecoder = decoder.beginStructure(descriptor)
        if (compositeDecoder.decodeSequentially()) TODO()
        else while (true) {
            when (val index = compositeDecoder.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break
                else -> ret = ret.conj(
                    compositeDecoder.decodeSerializableElement(
                        descriptor,
                        index,
                        eSerializer
                    )
                )
            }
        }
        compositeDecoder.endStructure(descriptor)
        return ret
    }

    override fun serialize(encoder: Encoder, value: PersistentVector<E>) {
        val size = value.count
        encoder.encodeCollection(descriptor, size) {
            val iterator = value.iterator()
            for (index in 0 until size)
                encodeSerializableElement(
                    descriptor,
                    index,
                    eSerializer,
                    iterator.next()
                )
        }
    }
}
