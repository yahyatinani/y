package io.github.yahyatinani.y.core.collections

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal const val PERSISTENT_VECTOR_NAME =
  "io.github.yahyatinani.y.core.collections.PersistentVector"

class PersistentVectorSerializer<E>(private val eSerializer: KSerializer<E>) :
  KSerializer<PersistentVector<E>> {

  private val listSerializer = ListSerializer(eSerializer)

  @OptIn(ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor = SerialDescriptor(
    PERSISTENT_VECTOR_NAME,
    listSerializer.descriptor,
  )

  override fun deserialize(decoder: Decoder): PersistentVector<E> =
    deserializePersistentCollection(
      decoder,
      descriptor,
      PersistentVector<E>(),
    ) { compositeDecoder, index, _ ->
      compositeDecoder.decodeSerializableElement(
        descriptor,
        index,
        eSerializer,
      )
    } as PersistentVector<E>

  override fun serialize(encoder: Encoder, value: PersistentVector<E>) =
    listSerializer.serialize(encoder, value)
}
