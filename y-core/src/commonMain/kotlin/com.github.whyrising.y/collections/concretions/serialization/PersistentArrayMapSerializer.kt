package com.github.whyrising.y.collections.concretions.serialization

import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap.Companion.EmptyArrayMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

const val PERSISTENT_ARRAY_MAP_NAME =
    "com.github.whyrising.y.collections.concretions.map.PersistentArrayMap"

internal class PersistentArrayMapSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : KSerializer<PersistentArrayMap<K, V>> {
    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor(
        PERSISTENT_ARRAY_MAP_NAME,
        mapSerializer.descriptor
    )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): PersistentArrayMap<K, V> =
        deserializePersistentCollection(
            decoder,
            descriptor,
            EmptyArrayMap
        ) { compositeDecoder, index, map ->
            decodeToMapEntry(
                compositeDecoder,
                descriptor,
                keySerializer,
                valueSerializer,
                index,
                map as PersistentArrayMap<K, V>
            )
        } as PersistentArrayMap<K, V>

    override fun serialize(encoder: Encoder, value: PersistentArrayMap<K, V>) =
        mapSerializer.serialize(encoder, value)
}
