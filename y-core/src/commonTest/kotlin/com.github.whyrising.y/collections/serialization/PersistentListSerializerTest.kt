package com.github.whyrising.y.collections.serialization

import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.serialization.PersistentListSerializer
import com.github.whyrising.y.l
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
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
