package io.github.yahyatinani.y.core.collections.serialization

import io.github.yahyatinani.y.core.collections.PersistentList
import io.github.yahyatinani.y.core.collections.PersistentListSerializer
import io.github.yahyatinani.y.core.l
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistentListSerializerTest : FreeSpec({
  "serialize" {
    val expectedEncoding = Json.encodeToString(listOf(1, 2, 3, 4))

    val encodeToString = Json.encodeToString(l(1, 2, 3, 4))

    encodeToString shouldBe expectedEncoding
  }

  "deserialize" {
    val str = Json.encodeToString(listOf(1, 2, 3, 4))

    Json.decodeFromString<PersistentList<Int>>(str) shouldBe l(1, 2, 3, 4)
  }

  "descriptor" {
    val s = PersistentListSerializer(Int.serializer())

    s.descriptor shouldBeSameInstanceAs s.listSerializer.descriptor
  }
})
