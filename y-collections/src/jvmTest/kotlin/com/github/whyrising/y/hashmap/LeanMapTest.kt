package com.github.whyrising.y.hashmap

import com.github.whyrising.y.Box
import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.BitMapIndexedNode
import com.github.whyrising.y.LeanMap.BitMapIndexedNode.BMIN
import com.github.whyrising.y.LeanMap.BitMapIndexedNode.Companion.bitmapNodeIndex
import com.github.whyrising.y.LeanMap.BitMapIndexedNode.EmptyBitMapIndexedNode
import com.github.whyrising.y.LeanMap.Companion.bitpos
import com.github.whyrising.y.LeanMap.HashCollisionNode
import com.github.whyrising.y.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
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

        "mergeTwoKeyValuePairs()/pairToSubNode()/subNode()" - {
            """when currentKeyMask < newKeyMask, return a node containing both
               where current entry comes before the new entry
            """ {
                val shift = 5
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val currentKey = "4"
                val currentValue = 4
                val currentHash = hasheq(currentKey)
                val currentNode =
                    BMIN<String, Int>(isMutable, 0, 5, emptyArray()).assoc(
                        isMutable,
                        shift,
                        currentHash,
                        currentKey,
                        currentValue,
                        leafFlag) as BitMapIndexedNode<String, Int>

                val newKey = "8"
                val newHash = hasheq(newKey)
                val newValue = 8
                val newDatamap = bitpos(currentHash, shift) or
                    bitpos(newHash, shift)

                val subNode = currentNode.mergeIntoSubNode(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    newHash,
                    newKey,
                    newValue) as BitMapIndexedNode<String, Int>
                val array = subNode.array

                subNode.isMutable shouldBeSameInstanceAs isMutable
                subNode.nodemap shouldBeExactly 0
                subNode.datamap shouldBeExactly newDatamap
                array.size shouldBeExactly 4
                array[0] shouldBeSameInstanceAs currentKey
                array[1] shouldBe currentValue
                array[2] shouldBeSameInstanceAs newKey
                array[3] shouldBe newValue
            }

            """when newKeyMask < currentKeyMask, return a node containing both
               where new entry comes before the current entry
            """ {
                val shift = 5
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val currentKey = "12"
                val currentValue = 12
                val currentHash = hasheq(currentKey)
                val currentNode =
                    BMIN<String, Int>(isMutable, 0, 5, emptyArray()).assoc(
                        isMutable,
                        shift,
                        currentHash,
                        currentKey,
                        currentValue,
                        leafFlag) as BitMapIndexedNode<String, Int>

                val newKey = "18"
                val newHash = hasheq(newKey)
                val newValue = 18
                val newDatamap = bitpos(currentHash, shift) or
                    bitpos(newHash, shift)

                val subNode = currentNode.mergeIntoSubNode(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    newHash,
                    newKey,
                    newValue) as BitMapIndexedNode<String, Int>
                val array = subNode.array

                subNode.isMutable shouldBeSameInstanceAs isMutable
                subNode.nodemap shouldBeExactly 0
                subNode.datamap shouldBeExactly newDatamap
                array.size shouldBeExactly 4
                array[0] shouldBeSameInstanceAs newKey
                array[1] shouldBe newValue
                array[2] shouldBeSameInstanceAs currentKey
                array[3] shouldBe currentValue
            }

            @Suppress("UNCHECKED_CAST")
            "when newKeyMask = currentKeyMask, it should subNode again" {
                val shift = 10
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val currentKey = "410"
                val currentValue = 410
                val currentHash = hasheq(currentKey)
                val currentNode =
                    BMIN<String, Int>(isMutable, 0, 0, emptyArray()).assoc(
                        isMutable,
                        shift,
                        currentHash,
                        currentKey,
                        currentValue,
                        leafFlag) as BitMapIndexedNode<String, Int>

                val newShift = 15
                val newKey = "1140"
                val newHash = hasheq(newKey)
                val newValue = 1140
                val newDatamap = bitpos(currentHash, newShift) or
                    bitpos(newHash, newShift)
                val newNodemap = bitpos(currentHash, shift)

                val node = currentNode.mergeIntoSubNode(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    newHash,
                    newKey,
                    newValue) as BitMapIndexedNode<String, Int>
                val array = node.array
                val subNode = array[0] as BitMapIndexedNode<String, Int>
                val subNodeArray = subNode.array

                node.isMutable shouldBeSameInstanceAs isMutable
                node.nodemap shouldBeExactly newNodemap
                node.datamap shouldBeExactly 0
                array.size shouldBeExactly 1

                subNode.isMutable shouldBeSameInstanceAs isMutable
                subNode.nodemap shouldBeExactly 0
                subNode.datamap shouldBeExactly newDatamap
                subNodeArray.size shouldBeExactly 4
                subNodeArray[0] shouldBeSameInstanceAs currentKey
                subNodeArray[1] shouldBe currentValue
                subNodeArray[2] shouldBeSameInstanceAs newKey
                subNodeArray[3] shouldBe newValue
            }

            """when hash collision and shift > 32, it should return 
               a HashCollisionNode""" {
                val shift = 35
                val isMutable = atomic(true)
                val currentKey = "RwtM1oQGxE"
                val currentValue = 158135874
                val currentHash = hasheq(currentKey)
                val leafFlag = Box(null)

                val currentNode =
                    BMIN<String, Int>(isMutable, 0, 0, emptyArray()).assoc(
                        isMutable,
                        shift,
                        currentHash,
                        currentKey,
                        currentValue,
                        leafFlag) as BitMapIndexedNode<String, Int>

                val newKey = "J2RCvlt3yJ"
                val newValue = 848159417
                val newHash = hasheq(newKey)

                val collisionNode =
                    currentNode.mergeIntoSubNode(
                        isMutable,
                        shift,
                        currentHash,
                        currentKey,
                        currentValue,
                        newHash,
                        newKey,
                        newValue) as HashCollisionNode<String, Int>
                val array = collisionNode.array

                collisionNode.isMutable shouldBeSameInstanceAs isMutable
                collisionNode.count shouldBeExactly 2
                collisionNode.hash shouldBeExactly currentHash
                array.size shouldBeExactly 4
                array[0] shouldBeSameInstanceAs currentKey
                array[1] shouldBe currentValue
                array[2] shouldBeSameInstanceAs newKey
                array[3] shouldBe newValue
            }
        }

        "updateArrayByIndex(index, value)" - {
            "when isMutable flags are not the same, return a new node updated" {
                val shift = 0
                val leafFlag = Box(null)
                val node = BitMapIndexedNode<Number, String>()
                    .assoc(atomic(true), shift, hasheq(4), 4, "4", leafFlag)
                    as BitMapIndexedNode<Number, String>

                val newNode = node.updateArrayByIndex(1, "z", atomic(true))

                newNode shouldNotBeSameInstanceAs node
                newNode.datamap shouldBeExactly node.datamap
                newNode.nodemap shouldBeExactly node.nodemap
                newNode.array.size shouldBeExactly 2
                newNode.array[0] shouldBe 4
                newNode.array[1] shouldBe "z"
            }

            """when isMutable flags are the same but set to false, 
               return a new node updated""" {
                val shift = 0
                val leafFlag = Box(null)
                val isMutable = atomic(false)
                val node = BitMapIndexedNode<Number, String>()
                    .assoc(isMutable, shift, hasheq(4), 4, "4", leafFlag)
                    as BitMapIndexedNode<Number, String>

                val newNode = node.updateArrayByIndex(1, "z", isMutable)

                newNode shouldNotBeSameInstanceAs node
                newNode.datamap shouldBeExactly node.datamap
                newNode.nodemap shouldBeExactly node.nodemap
                newNode.array.size shouldBeExactly 2
                newNode.array[0] shouldBe 4
                newNode.array[1] shouldBe "z"
            }

            "when it's allowed to mutate, update the value in array" {
                val shift = 0
                val leafFlag = Box(null)
                val isMutable = atomic(true)
                val node = BitMapIndexedNode<Number, String>()
                    .assoc(isMutable, shift, hasheq(4), 4, "4", leafFlag)
                    as BitMapIndexedNode<Number, String>

                val newNode = node.updateArrayByIndex(1, "z", isMutable)

                newNode shouldBeSameInstanceAs node
                newNode.array.size shouldBeExactly 2
                newNode.array[0] shouldBe 4
                newNode.array[1] shouldBe "z"
            }
        }

        "assoc(isMutable, shift, keyHash, pair, leafFlag)" - {
            """when collision-free, it should add the pair to the first half 
               of the array""" {
                val key = "a"
                val isMutable = atomic(true)
                val shift = 0
                val keyHash = hasheq(key)
                val value = 18
                val leafFlag = Box(null)
                val node = BitMapIndexedNode<String, Int>()

                val newNode = node.assoc(
                    isMutable, shift, keyHash, key, value, leafFlag
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

            @Suppress("UNCHECKED_CAST")
            "when there is a collision in the first half of the array" - {
                "when keys are not equiv, it should call mergeIntoSubNode()" {
                    val key = "8"
                    val value = 8
                    val isMutable = atomic(true)
                    val shift = 0
                    val keyHash = hasheq(key)
                    val leafFlag = Box(null)
                    val bitpos = bitpos(keyHash, shift)
                    val node = BitMapIndexedNode<String, Int>()
                        .assoc(isMutable, shift, hasheq("0"), "0", 0, leafFlag)
                        .assoc(isMutable, shift, hasheq("2"), "2", 2, leafFlag)
                        .assoc(isMutable, shift, hasheq("4"), "4", 4, leafFlag)
                        as BitMapIndexedNode<String, Int>

                    val newNode = node
                        .assoc(isMutable, shift, keyHash, key, value, leafFlag)
                        as BitMapIndexedNode<String, Int>

                    val newArray = newNode.array
                    val subNode = newArray[4] as BitMapIndexedNode<String, Int>

                    leafFlag.value shouldBeSameInstanceAs leafFlag
                    newArray.size shouldBeExactly 5

                    newNode.datamap shouldBeExactly (node.datamap xor bitpos)
                    newNode.nodemap shouldBeExactly (node.nodemap or bitpos)

                    newArray[0] shouldBe "0"
                    newArray[1] shouldBe 0
                    newArray[2] shouldBe "2"
                    newArray[3] shouldBe 2

                    subNode.array.size shouldBeExactly 4
                    subNode.array[0] shouldBe "4"
                    subNode.array[1] shouldBe 4
                    subNode.array[2] shouldBe "8"
                    subNode.array[3] shouldBe 8
                }

                "when keys are equiv" - {
                    "when it's allowed to mutate, update the value in array" {
                        val shift = 0
                        val isMutable = atomic(true)
                        val leafFlag = Box(null)
                        val node = BitMapIndexedNode<Number, String>()
                            .assoc(
                                isMutable, shift, hasheq(4), 4, "4", leafFlag)
                            as BitMapIndexedNode<Number, String>

                        val newNode = node
                            .assoc(
                                isMutable, shift, hasheq(4L), 4L, "1", leafFlag)
                            as BitMapIndexedNode<String, Int>

                        newNode shouldBeSameInstanceAs node
                        newNode.array.size shouldBeExactly 2
                        newNode.array[0] shouldBe 4
                        newNode.array[1] shouldBe "1"
                    }

                    "when it's not allowed to mutate, return new node updated" {
                        val shift = 0
                        val leafFlag = Box(null)
                        val node = BitMapIndexedNode<Number, String>()
                            .assoc(
                                atomic(true),
                                shift,
                                hasheq(4),
                                4,
                                "4",
                                leafFlag) as BitMapIndexedNode<Number, String>

                        val newNode = node
                            .assoc(
                                atomic(true),
                                shift,
                                hasheq(4L),
                                4L,
                                "1",
                                leafFlag) as BitMapIndexedNode<String, Int>

                        node.array.size shouldBeExactly 2
                        node.array[0] shouldBe 4
                        node.array[1] shouldBe "4"
                        newNode shouldNotBeSameInstanceAs node
                        newNode.datamap shouldBeExactly node.datamap
                        newNode.nodemap shouldBeExactly node.nodemap
                        newNode.array.size shouldBeExactly 2
                        newNode.array[0] shouldBe 4
                        newNode.array[1] shouldBe "1"
                    }
                }
            }

            "when there is collision in the second half of the array" - {
                @Suppress("UNCHECKED_CAST")
                "it should add to the subNode and return a new node" {
                    val shift = 0
                    val isMutable = atomic(true)
                    val leafFlag = Box(null)

                    var i = 0
                    var node = BitMapIndexedNode<String, Int>()
                    while (i < 36) {
                        val key = "$i"
                        node = node.assoc(
                            isMutable, shift, hasheq(key), key, i, leafFlag
                        ) as BitMapIndexedNode<String, Int>
                        i += 1
                    }

                    val newNode = node.assoc(
                        isMutable, shift, hasheq("36"), "36", 36, leafFlag)
                        as BitMapIndexedNode<String, Int>
                    val newArray = newNode.array
                    val subNode = newArray[24] as BitMapIndexedNode<String, Int>

                    newNode shouldBeSameInstanceAs node
                    newArray.size shouldBeExactly node.array.size
                    subNode.array[2] shouldBe "36"
                    subNode.array[3] shouldBe 36
                }

                @Suppress("UNCHECKED_CAST")
                "it should put the subNode and return this node" {
                    val shift = 0
                    val isMutable = atomic(true)
                    val leafFlag = Box(null)

                    var i = 0
                    var node = BitMapIndexedNode<String, Int>()
                    while (i < 424) {
                        val key = "$i"
                        node = node.assoc(
                            isMutable, shift, hasheq(key), key, i, leafFlag
                        ) as BitMapIndexedNode<String, Int>
                        i += 2
                    }
                    val array =
                        ((node.array[5] as BitMapIndexedNode<String, Int>)
                            .array[12] as BitMapIndexedNode<String, Int>).array

                    val newNode = node.assoc(
                        isMutable, shift, hasheq("424"), "424", 424, leafFlag)
                        as BitMapIndexedNode<String, Int>
                    val newArray =
                        ((newNode.array[5] as BitMapIndexedNode<String, Int>)
                            .array[12] as BitMapIndexedNode<String, Int>).array

                    newNode shouldBeSameInstanceAs node
                    newArray.size shouldBeExactly array.size + 2
                    newArray[4] shouldBe "424"
                    newArray[5] shouldBe 424
                }
            }
        }

        "hasNodes()" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            val node = BitMapIndexedNode<String, Int>()

            var i = 0
            var newNode = node
            while (i < 36) {
                val key = "$i"
                newNode = newNode.assoc(
                    isMutable, shift, hasheq(key), key, i, leafFlag
                ) as BitMapIndexedNode<String, Int>
                i += 1
            }

            node.hasNodes().shouldBeFalse()
            newNode.hasNodes().shouldBeTrue()
        }
    }
})
