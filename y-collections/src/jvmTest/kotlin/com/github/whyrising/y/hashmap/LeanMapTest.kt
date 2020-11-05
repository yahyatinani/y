package com.github.whyrising.y.hashmap

import com.github.whyrising.y.Box
import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.BitMapIndexedNode
import com.github.whyrising.y.LeanMap.BitMapIndexedNode.Companion.bitmapNodeIndex
import com.github.whyrising.y.LeanMap.BitMapIndexedNode.EmptyBitMapIndexedNode
import com.github.whyrising.y.LeanMap.Companion.bitpos
import com.github.whyrising.y.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.atomicfu.atomic

@ExperimentalStdlibApi
class LeanMapTest : FreeSpec({
    "mask(hash, shift)" {
        LeanMap.mask(hasheq("a"), 0) shouldBeExactly 17
        LeanMap.mask(hasheq("b"), 0) shouldBeExactly 22
    }

    "bitpos(hash, shift)" {
        bitpos(hasheq("b"), 0) shouldBeExactly 4194304
        bitpos(hasheq("c"), 0) shouldBeExactly 1073741824
        bitpos(hasheq("d"), 0) shouldBeExactly 1
    }

    "BitMapIndexedNodeTest" - {
        "EmptyBitMapIndexedNode" {
            EmptyBitMapIndexedNode.isMutable.value.shouldBeFalse()
            EmptyBitMapIndexedNode.datamap shouldBeExactly 0
            EmptyBitMapIndexedNode.nodemap shouldBeExactly 0
            EmptyBitMapIndexedNode.array.size shouldBeExactly 0
            (45 and (78 - 1)).countOneBits()
        }

        "bitmapNodeIndex(bitmap, bitpos)" {
            bitmapNodeIndex(0, 4194304) shouldBeExactly 0
            bitmapNodeIndex(4194304, 1073741824) shouldBeExactly 1
            bitmapNodeIndex(1077936128, 1) shouldBeExactly 0
        }

        "assoc(isMutable, shift, keyHash, pair, leafFlag)" - {
            """when collision-free, it should add the pair to the first half 
               of the array""" {
                val key = "a"
                val isMutable = atomic(true)
                val shift = 0
                val keyHash = hasheq(key)
                val value = 18
                val keyVal = Pair(key, value)
                val leafFlag = Box(null)
                val node = BitMapIndexedNode<String, Int>()

                val newNode = node.assoc(
                    isMutable, shift, keyHash, keyVal, leafFlag
                ) as BitMapIndexedNode<String, Int>
                val newArray = newNode.array

                newNode.isMutable shouldBeSameInstanceAs isMutable
                newNode.datamap shouldBeExactly
                    (node.datamap or bitpos(keyHash, shift))
                newNode.nodemap shouldBeExactly node.nodemap
                leafFlag.value shouldBeSameInstanceAs leafFlag
                newArray.size shouldBeExactly node.array.size + 2
                newArray[0] shouldBe key
                newArray[1] shouldBe value
            }

            "" {

            }
        }
    }
})
