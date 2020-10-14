package com.github.whyrising.y

import com.github.whyrising.y.PersistentVector.EmptyPersistentVector
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class NodeTest : FreeSpec({
    @Suppress("UNCHECKED_CAST")
    "Node does have an array" {
        val array = arrayOfNulls<Int>(33)

        val node = PersistentVector.Node<Int>(array as Array<Any?>)

        node.array shouldBeSameInstanceAs array
    }
})

class PersistentVectorTest : FreeSpec({

    "PersistentVector" - {
        "invoke() should return the EmptyPersistentVector" {
            PersistentVector<Int>() shouldBe EmptyPersistentVector
        }
    }

    "EmptyPersistentVector" - {
        "toString() should return []" {
            PersistentVector<Int>().toString() shouldBe "[]"
        }

        "count should be 0" {
            PersistentVector<Int>().count shouldBeExactly 0
        }

        "shift should be 5" {
            PersistentVector<Int>().shift shouldBeExactly 5
        }

        "tail should be an empty array of size 0" {
            val tail = PersistentVector<Int>().tail

            tail.size shouldBeExactly 0
            shouldThrowExactly<ArrayIndexOutOfBoundsException> {
                tail[0]
            }
        }

        "root should be an empty node of size 32" {
            val array = PersistentVector<Int>().root.array

            array.size shouldBeExactly 32
            array[0].shouldBeNull()
            array[31].shouldBeNull()
        }
    }
})
