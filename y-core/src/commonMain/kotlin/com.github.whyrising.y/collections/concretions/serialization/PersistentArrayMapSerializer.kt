package com.github.whyrising.y.collections.concretions.serialization

import com.github.whyrising.y.collections.concretions.map.MapEntry
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap.Companion.EmptyArrayMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
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
    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): PersistentArrayMap<K, V> =
        deserializePersistentCollection(
            decoder,
            descriptor,
            EmptyArrayMap
        ) { compositeDecoder, index, map ->
            map as PersistentArrayMap<K, V>
            val key: K = compositeDecoder.decodeSerializableElement(
                descriptor,
                index,
                keySerializer
            )
            val vIndex = compositeDecoder.decodeElementIndex(descriptor).also {
                require(it == index + 1) {
                    "Value must follow key in a map, index for key: $index, " +
                        "returned index for value: $it"
                }
            }
            val value: V = if (map.containsKey(key) &&
                valueSerializer.descriptor.kind !is PrimitiveKind
            ) compositeDecoder.decodeSerializableElement(
                descriptor,
                vIndex,
                valueSerializer,
                map.valAt(key)
            ) else compositeDecoder.decodeSerializableElement(
                descriptor,
                vIndex,
                valueSerializer
            )
            MapEntry(key, value)
        } as PersistentArrayMap<K, V>

    override fun serialize(encoder: Encoder, value: PersistentArrayMap<K, V>) {
        mapSerializer.serialize(encoder, value)
    }
}
