package com.github.whyrising.y.collections.serialization

import com.github.whyrising.y.collections.concretions.serialization.PERSISTENT_HASH_SET_NAME
import com.github.whyrising.y.collections.concretions.serialization.PersistentHashSetSerializer
import com.github.whyrising.y.collections.concretions.set.PersistentHashSet
import com.github.whyrising.y.hs
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
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
