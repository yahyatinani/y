package com.github.whyrising.y.collections.concretions.serialization

import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

const val PERSISTENT_HASH_MAP_NAME =
    "com.github.whyrising.y.collections.concretions.map.PersistentHashMap"

internal class PersistentHashMapSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : KSerializer<PersistentHashMap<K, V>> {
    private val mapSerializer = MapSerializer(keySerializer, valueSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = SerialDescriptor(
        PERSISTENT_HASH_MAP_NAME,
        mapSerializer.descriptor
    )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): PersistentHashMap<K, V> =
        deserializePersistentCollection(
            decoder,
            descriptor,
            PersistentHashMap.EmptyHashMap
        ) { compositeDecoder, index, map ->
            decodeToMapEntry(
                compositeDecoder,
                descriptor,
                keySerializer,
                valueSerializer,
                index,
                map as PersistentHashMap<K, V>
            )
        } as PersistentHashMap<K, V>

    override fun serialize(encoder: Encoder, value: PersistentHashMap<K, V>) =
        mapSerializer.serialize(encoder, value)
}
