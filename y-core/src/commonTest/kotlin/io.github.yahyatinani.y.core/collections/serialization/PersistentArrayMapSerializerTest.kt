package io.github.yahyatinani.y.core.collections.serialization

import io.github.yahyatinani.y.core.collections.PERSISTENT_ARRAY_MAP_NAME
import io.github.yahyatinani.y.core.collections.PersistentArrayMap
import io.github.yahyatinani.y.core.collections.PersistentArrayMapSerializer
import io.github.yahyatinani.y.core.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class PersistentArrayMapSerializerTest : FreeSpec({
  "serialName" {
    PERSISTENT_ARRAY_MAP_NAME shouldBe
      PersistentArrayMap::class.qualifiedName
  }

  "discriptor" {
    val serializer =
      PersistentArrayMapSerializer(String.serializer(), Int.serializer())

    serializer.descriptor.serialName shouldBe PERSISTENT_ARRAY_MAP_NAME
  }

  "serialize" {
    val expected = Json.encodeToString(mapOf("a" to 1, "b" to 2, "c" to 3))

    val map = m("a" to 1, "b" to 2, "c" to 3) as PersistentArrayMap<String, Int>

    Json.encodeToString(map) shouldBe expected
  }

  "deserialize" {
    val str = Json.encodeToString(mapOf("a" to 1, "b" to 2, "c" to 3))

    val map = Json.decodeFromString<PersistentArrayMap<String, Int>>(str)

    map shouldBe m("a" to 1, "b" to 2, "c" to 3)
  }
})
