package com.github.whyrising.y.collections.serialization

import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import com.github.whyrising.y.collections.concretions.serialization.PERSISTENT_HASH_MAP_NAME
import com.github.whyrising.y.collections.concretions.serialization.PersistentHashMapSerializer
import com.github.whyrising.y.hashMap
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class PersistentHashMapSerializerTest : FreeSpec({
    "serialName" {
        PERSISTENT_HASH_MAP_NAME shouldBe PersistentHashMap::class.qualifiedName
    }

    "discriptor" {
        val serializer =
            PersistentHashMapSerializer(String.serializer(), Int.serializer())

        serializer.descriptor.serialName shouldBe PERSISTENT_HASH_MAP_NAME
    }

    "serialize" {
        val expected = Json.encodeToString(mapOf("a" to 1, "b" to 2, "c" to 3))

        val hashmap = hashMap("a" to 1, "b" to 2, "c" to 3)

        Json.encodeToString(hashmap) shouldBe expected
    }

    "deserialize" {
        val encoded = Json.encodeToString(mapOf("a" to 1, "b" to 2, "c" to 3))

        val map = Json.decodeFromString<PersistentHashMap<String, Int>>(encoded)

        map shouldBe hashMap("a" to 1, "b" to 2, "c" to 3)
    }
})
