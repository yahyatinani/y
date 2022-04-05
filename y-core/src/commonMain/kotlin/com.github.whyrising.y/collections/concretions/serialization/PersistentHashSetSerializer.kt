package com.github.whyrising.y.collections.concretions.serialization

import com.github.whyrising.y.collections.concretions.set.PersistentHashSet
import com.github.whyrising.y.hs
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeCollection

internal const val PERSISTENT_HASH_SET_NAME =
    "com.github.whyrising.y.collections.concretions.set.PersistentHashSet"

internal class PersistentHashSetSerializer<E>(
    private val eSerializer: KSerializer<E>
) : KSerializer<PersistentHashSet<E>> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor(
        PERSISTENT_HASH_SET_NAME,
        SetSerializer(eSerializer).descriptor
    )

    override fun deserialize(decoder: Decoder): PersistentHashSet<E> =
        deserializePersistentCollection(
            decoder,
            descriptor,
            hs<E>()
        ) { compositeDecoder, index, _ ->
            compositeDecoder.decodeSerializableElement(
                descriptor,
                index,
                eSerializer
            )
        } as PersistentHashSet<E>

    override fun serialize(encoder: Encoder, value: PersistentHashSet<E>) {
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
