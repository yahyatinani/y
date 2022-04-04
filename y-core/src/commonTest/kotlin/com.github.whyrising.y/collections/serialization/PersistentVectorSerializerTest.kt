package com.github.whyrising.y.collections.serialization

import com.github.whyrising.y.collections.concretions.serialization.PERSISTENT_VECTOR_NAME
import com.github.whyrising.y.collections.concretions.serialization.PersistentVectorSerializer
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.v
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class PersistentVectorSerializerTest : FreeSpec({
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

    "descriptor" {
        val e = Int.serializer()
        val serializer = PersistentVectorSerializer(e)

        serializer.descriptor.serialName shouldBe PERSISTENT_VECTOR_NAME
    }
})
