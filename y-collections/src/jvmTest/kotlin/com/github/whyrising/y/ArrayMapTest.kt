package com.github.whyrising.y

import com.github.whyrising.y.PersistentArrayMap.ArrayMap
import com.github.whyrising.y.PersistentArrayMap.EmptyArrayMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ArrayMapTest : FreeSpec({
    "ArrayMap" - {
        "invoke() should return EmptyArrayMap" {
            PersistentArrayMap<String, Int>() shouldBeSameInstanceAs
                EmptyArrayMap
        }

        "invoke(pairs)" - {
            "it should return an ArrayMap" {
                val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

                val map = PersistentArrayMap(*array) as ArrayMap<String, Int>
                val pairs = map.pairs

                pairs shouldBe array
            }

            "when duplicate keys, it should throw an exception" {
                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap("a" to 1, "b" to 2, "b" to 3)
                }
                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap("a" to 1, "a" to 2, "b" to 3)
                }
                shouldThrowExactly<IllegalArgumentException> {
                    PersistentArrayMap("a" to 1, "b" to 2, "a" to 3)
                }
            }
        }
    }

    "EmptyArrayMap" - {
        "toString() should return `{}`" {
            PersistentArrayMap<String, Int>().toString() shouldBe "{}"
        }
    }
})
