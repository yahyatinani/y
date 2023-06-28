package io.github.yahyatinani.y.core.collections.serialization

import io.github.yahyatinani.y.core.collections.PERSISTENT_HASH_SET_NAME
import io.github.yahyatinani.y.core.collections.PersistentHashSet
import io.github.yahyatinani.y.core.collections.PersistentHashSetSerializer
import io.github.yahyatinani.y.core.hs
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class PersistentHashSetSerializerTest : FreeSpec({
  "serialName" {
    PERSISTENT_HASH_SET_NAME shouldBe PersistentHashSet::class.qualifiedName
  }

  "descriptor" {
    val serializer = PersistentHashSetSerializer(Int.serializer())

    serializer.descriptor.serialName shouldBe PERSISTENT_HASH_SET_NAME
  }

  "serialize" {
    Json.encodeToString(hs(1, 2, 3, 4)) shouldBe "[1,4,3,2]"
  }

  "deserialize" {
    val set = Json.decodeFromString<PersistentHashSet<Int>>("[1,2,3,4]")

    set shouldBe hs(1, 2, 3, 4)
  }
})
