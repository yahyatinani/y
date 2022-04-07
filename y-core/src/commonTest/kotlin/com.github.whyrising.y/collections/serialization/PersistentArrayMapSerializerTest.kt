package com.github.whyrising.y.collections.serialization

import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.serialization.PERSISTENT_ARRAY_MAP_NAME
import com.github.whyrising.y.collections.concretions.serialization.PersistentArrayMapSerializer
import com.github.whyrising.y.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
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

        val map = m("a" to 1, "b" to 2, "c" to 3)

        Json.encodeToString(map) shouldBe expected
    }

    "deserialize" {
        val str = Json.encodeToString(mapOf("a" to 1, "b" to 2, "c" to 3))

        val map = Json.decodeFromString<PersistentArrayMap<String, Int>>(str)

        map shouldBe m("a" to 1, "b" to 2, "c" to 3)
    }
})