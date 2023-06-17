package com.github.whyrising.y.core.collections

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

@OptIn(ExperimentalSerializationApi::class)
fun <E> deserializePersistentCollection(
  decoder: Decoder,
  descriptor: SerialDescriptor,
  coll: IPersistentCollection<E>,
  decodeElement: (CompositeDecoder, index: Int, IPersistentCollection<E>) -> E,
): IPersistentCollection<E> {
  tailrec fun decode(
    acc: IPersistentCollection<E>,
    compositeDecoder: CompositeDecoder,
  ): IPersistentCollection<E> = when {
    compositeDecoder.decodeSequentially() -> TODO()
    else -> when (val i = compositeDecoder.decodeElementIndex(descriptor)) {
      CompositeDecoder.DECODE_DONE -> acc
      else -> decode(
        acc.conj(decodeElement(compositeDecoder, i, acc)),
        compositeDecoder,
      )
    }
  }

  val compositeDecoder = decoder.beginStructure(descriptor)
  val ret = when {
    compositeDecoder.decodeSequentially() -> TODO()
    else -> decode(coll, compositeDecoder)
  }
  compositeDecoder.endStructure(descriptor)
  return ret
}

@OptIn(ExperimentalSerializationApi::class)
fun <K, V> decodeToMapEntry(
  compositeDecoder: CompositeDecoder,
  descriptor: SerialDescriptor,
  keySerializer: KSerializer<K>,
  valueSerializer: KSerializer<V>,
  index: Int,
  map: IPersistentMap<K, V>,
): MapEntry<K, V> {
  val key: K = compositeDecoder.decodeSerializableElement(
    descriptor,
    index,
    keySerializer,
  )
  val vIndex = compositeDecoder.decodeElementIndex(descriptor).also {
    require(it == index + 1) {
      "Value must follow key in a map, index for key: $index, " +
        "returned index for value: $it"
    }
  }
  val value: V = if (map.containsKey(key) &&
    valueSerializer.descriptor.kind !is PrimitiveKind
  ) {
    compositeDecoder.decodeSerializableElement(
      descriptor,
      vIndex,
      valueSerializer,
      map.valAt(key),
    )
  } else compositeDecoder.decodeSerializableElement(
    descriptor,
    vIndex,
    valueSerializer,
  )
  return MapEntry(key, value)
}
