package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.toPlist
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// TODO: 4/4/22 Add documentation of O(n) complexity for this serializer.
internal class PersistentListSerializer<E>(element: KSerializer<E>) :
  KSerializer<PersistentList<E>> {

  internal val listSerializer = ListSerializer(element)

  override val descriptor: SerialDescriptor = listSerializer.descriptor

  override fun deserialize(decoder: Decoder): PersistentList<E> =
    listSerializer.deserialize(decoder).toPlist()

  override fun serialize(encoder: Encoder, value: PersistentList<E>) =
    listSerializer.serialize(encoder, value)
}
