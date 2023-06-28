package io.github.yahyatinani.y.core.collections.serialization

import io.github.yahyatinani.y.core.collections.PERSISTENT_VECTOR_NAME
import io.github.yahyatinani.y.core.collections.PersistentVector
import io.github.yahyatinani.y.core.collections.PersistentVectorSerializer
import io.github.yahyatinani.y.core.v
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class PersistentVectorSerializerTest : FreeSpec({
  "serialName" {
    PERSISTENT_VECTOR_NAME shouldBe PersistentVector::class.qualifiedName
  }

  "descriptor" {
    val serializer = PersistentVectorSerializer(Int.serializer())

    serializer.descriptor.serialName shouldBe PERSISTENT_VECTOR_NAME
  }

  "serialize" {
    val expectedEncoding = Json.encodeToString(arrayOf(1, 2, 3, 4))

    val serialized = Json.encodeToString(PersistentVector(1, 2, 3, 4))

    serialized shouldBe expectedEncoding
  }

  "deserialize" {
    val serializedArray = Json.encodeToString(arrayOf(1, 2, 3, 4))

    val vec = Json.decodeFromString<PersistentVector<Int>>(serializedArray)

    vec shouldBe v(1, 2, 3, 4)
  }
})
