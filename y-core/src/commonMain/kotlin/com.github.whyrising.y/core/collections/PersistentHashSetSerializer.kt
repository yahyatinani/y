package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.hs
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal const val PERSISTENT_HASH_SET_NAME =
  "com.github.whyrising.y.core.collections.PersistentHashSet"

internal class PersistentHashSetSerializer<E>(
  private val eSerializer: KSerializer<E>,
) : KSerializer<PersistentHashSet<E>> {
  private val setSerializer = SetSerializer(eSerializer)

  @OptIn(ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor = SerialDescriptor(
    PERSISTENT_HASH_SET_NAME,
    setSerializer.descriptor,
  )

  override fun deserialize(decoder: Decoder): PersistentHashSet<E> =
    deserializePersistentCollection(
      decoder,
      descriptor,
      hs<E>(),
    ) { compositeDecoder, index, _ ->
      compositeDecoder.decodeSerializableElement(
        descriptor,
        index,
        eSerializer,
      )
    } as PersistentHashSet<E>

  override fun serialize(encoder: Encoder, value: PersistentHashSet<E>) =
    setSerializer.serialize(encoder, value)
}
