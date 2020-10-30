package com.github.whyrising.y

import com.github.whyrising.y.APersistentMap.KeySeq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@Suppress("UNCHECKED_CAST")
class APersistentMapTest : FreeSpec({
    "KeySeq" - {
        val map = am("a" to 1, "b" to 2, "c" to 3)

        "KeySeq should be a seq" {
            val keySeq: ISeq<String> = KeySeq(map)
            val rest = keySeq.rest() as KeySeq<String, Int>

            keySeq.count shouldBeExactly map.size

            keySeq.first() shouldBe "a"

            rest.map.shouldBeNull()
            rest.count shouldBeExactly map.size - 1
            rest.first() shouldBe "b"
            rest.rest().first() shouldBe "c"
        }
    }
})
