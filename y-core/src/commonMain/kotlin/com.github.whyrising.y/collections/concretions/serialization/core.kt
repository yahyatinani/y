package com.github.whyrising.y.collections.concretions.serialization

import com.github.whyrising.y.collections.seq.IPersistentCollection
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

@OptIn(ExperimentalSerializationApi::class)
fun <E> deserializePersistentCollection(
    decoder: Decoder,
    descriptor: SerialDescriptor,
    coll: IPersistentCollection<E>,
    decodeElement: (CompositeDecoder, index: Int, IPersistentCollection<E>) -> E
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
                compositeDecoder
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
