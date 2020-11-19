package com.github.whyrising.y.map.hashmap

import com.github.whyrising.y.concretions.list.PersistentList
import com.github.whyrising.y.concretions.map.LeanMap
import com.github.whyrising.y.concretions.map.LeanMap.BitMapIndexedNode
import com.github.whyrising.y.concretions.map.LeanMap.BitMapIndexedNode.EmptyBitMapIndexedNode
import com.github.whyrising.y.concretions.map.LeanMap.Node
import com.github.whyrising.y.concretions.map.MapEntry
import com.github.whyrising.y.util.Box
import com.github.whyrising.y.util.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.atomicfu.atomic

@ExperimentalStdlibApi
class BitMapIndexedNodeTest : FreeSpec({
    "EmptyBitMapIndexedNode" {
        EmptyBitMapIndexedNode.isMutable.value.shouldBeFalse()
        EmptyBitMapIndexedNode.datamap shouldBeExactly 0
        EmptyBitMapIndexedNode.nodemap shouldBeExactly 0
        EmptyBitMapIndexedNode.array.size shouldBeExactly 0
        (45 and (78 - 1)).countOneBits()
    }

    "bitmapNodeIndex(bitmap, bitpos)" {
        BitMapIndexedNode.bitmapNodeIndex(0, 4194304) shouldBeExactly 0
        BitMapIndexedNode.bitmapNodeIndex(4194304, 1073741824) shouldBeExactly 1
        BitMapIndexedNode.bitmapNodeIndex(1077936128, 1) shouldBeExactly 0
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
            val currentNode = BitMapIndexedNode
                .BMIN<String, Int>(isMutable, 0, 5, emptyArray()).assoc(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    leafFlag
                ) as BitMapIndexedNode<String, Int>

            val newKey = "8"
            val newHash = hasheq(newKey)
            val newValue = 8
            val newDatamap = LeanMap.bitpos(currentHash, shift) or
                LeanMap.bitpos(newHash, shift)

            val subNode = currentNode.mergeIntoSubNode(
                isMutable,
                shift,
                currentHash,
                currentKey,
                currentValue,
                newHash,
                newKey,
                newValue
            ) as BitMapIndexedNode<String, Int>
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
            val currentNode = BitMapIndexedNode
                .BMIN<String, Int>(isMutable, 0, 5, emptyArray()).assoc(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    leafFlag
                ) as BitMapIndexedNode<String, Int>

            val newKey = "18"
            val newHash = hasheq(newKey)
            val newValue = 18
            val newDatamap = LeanMap.bitpos(currentHash, shift) or
                LeanMap.bitpos(newHash, shift)

            val subNode = currentNode.mergeIntoSubNode(
                isMutable,
                shift,
                currentHash,
                currentKey,
                currentValue,
                newHash,
                newKey,
                newValue
            ) as BitMapIndexedNode<String, Int>
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
            val currentNode = BitMapIndexedNode
                .BMIN<String, Int>(isMutable, 0, 0, emptyArray()).assoc(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    leafFlag
                ) as BitMapIndexedNode<String, Int>

            val newShift = 15
            val newKey = "1140"
            val newHash = hasheq(newKey)
            val newValue = 1140
            val newDatamap = LeanMap.bitpos(currentHash, newShift) or
                LeanMap.bitpos(newHash, newShift)
            val newNodemap = LeanMap.bitpos(currentHash, shift)

            val node = currentNode.mergeIntoSubNode(
                isMutable,
                shift,
                currentHash,
                currentKey,
                currentValue,
                newHash,
                newKey,
                newValue
            ) as BitMapIndexedNode<String, Int>
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

            val currentNode = BitMapIndexedNode
                .BMIN<String, Int>(isMutable, 0, 0, emptyArray()).assoc(
                    isMutable,
                    shift,
                    currentHash,
                    currentKey,
                    currentValue,
                    leafFlag
                ) as BitMapIndexedNode<String, Int>

            val newKey = "J2RCvlt3yJ"
            val newValue = 848159417
            val newHash = hasheq(newKey)

            val collisionNode = currentNode.mergeIntoSubNode(
                isMutable,
                shift,
                currentHash,
                currentKey,
                currentValue,
                newHash,
                newKey,
                newValue
            ) as LeanMap.HashCollisionNode<String, Int>
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
                (node.datamap or LeanMap.bitpos(keyHash, shift))
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
                val bitpos = LeanMap.bitpos(keyHash, shift)
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
                    val node: Node<Number, String> =
                        BitMapIndexedNode<Number, String>().assoc(
                            isMutable, shift, hasheq(4), 4, "4", leafFlag
                        )

                    val newNode: Node<Number, String> = node.assoc(
                        isMutable, shift, hasheq(4L), 4L, "1", leafFlag
                    )

                    newNode shouldBeSameInstanceAs node
                    newNode.array.size shouldBeExactly 2
                    newNode.array[0] shouldBe 4
                    newNode.array[1] shouldBe "1"
                }

                "when it's not allowed to mutate, return new node updated" {
                    val shift = 0
                    val leafFlag = Box(null)
                    val node = BitMapIndexedNode<Number, String>().assoc(
                        atomic(true),
                        shift,
                        hasheq(4),
                        4,
                        "4",
                        leafFlag
                    ) as BitMapIndexedNode<Number, String>

                    val newNode = node.assoc(
                        atomic(true),
                        shift,
                        hasheq(4L),
                        4L,
                        "1",
                        leafFlag
                    ) as BitMapIndexedNode<String, Int>

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
                var node: Node<String, Int> = BitMapIndexedNode()
                while (i < 36) {
                    val key = "$i"
                    node = node.assoc(
                        isMutable, shift, hasheq(key), key, i, leafFlag
                    )
                    i += 1
                }

                val newNode = node.assoc(
                    isMutable, shift, hasheq("36"), "36", 36, leafFlag
                )
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
                var node: Node<String, Int> = BitMapIndexedNode()
                while (i < 424) {
                    val key = "$i"
                    node = node.assoc(
                        isMutable, shift, hasheq(key), key, i, leafFlag
                    )
                    i += 2
                }
                val array = (
                    (node.array[5] as BitMapIndexedNode<String, Int>)
                        .array[12] as BitMapIndexedNode<String, Int>
                    ).array

                val newNode = node.assoc(
                    isMutable, shift, hasheq("424"), "424", 424, leafFlag
                )
                val newArray = (
                    (newNode.array[5] as BitMapIndexedNode<String, Int>)
                        .array[12] as BitMapIndexedNode<String, Int>
                    ).array

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
        val m: Node<String, Int> = BitMapIndexedNode()
        var n = m

        var i = 0
        while (i < 36) {
            val key = "$i"
            n = n.assoc(isMutable, shift, hasheq(key), key, i, leafFlag)
            i++
        }

        m.hasNodes().shouldBeFalse()
        n.hasNodes().shouldBeTrue()
    }

    "hasData()" {
        val shift = 0
        val isMutable = atomic(true)
        val leafFlag = Box(null)
        val node: Node<String, Int> = BitMapIndexedNode()
        val n = node.assoc(isMutable, shift, hasheq("a"), "a", 15, leafFlag)

        node.hasData().shouldBeFalse()
        n.hasData().shouldBeTrue()
    }

    "nodeArity() should return the count of one bits in nodemap" {
        val shift = 0
        val isMutable = atomic(true)
        val leafFlag = Box(null)
        var i = 0
        var n: Node<String, Int> = BitMapIndexedNode()
        while (i < 20) {
            val key = "$i"
            n = n.assoc(isMutable, shift, hasheq(key), key, i, leafFlag)
            i += 2
        }

        n.nodeArity() shouldBeExactly 2
    }

    "dataArity() should return the count of one bits in datamap" {
        val shift = 0
        val isMutable = atomic(true)
        val leafFlag = Box(null)
        var i = 0
        var n: Node<String, Int> = BitMapIndexedNode()
        while (i < 20) {
            val key = "$i"
            n = n.assoc(isMutable, shift, hasheq(key), key, i, leafFlag)
            i += 2
        }

        n.dataArity() shouldBeExactly 6
    }

    "getNode(index) should return the nth node from the right of array" {
        val shift = 0
        val isMutable = atomic(true)
        val leafFlag = Box(null)
        var i = 0
        var n: Node<String, Int> = BitMapIndexedNode()
        while (i < 20) {
            val key = "$i"
            n = n.assoc(isMutable, shift, hasheq(key), key, i, leafFlag)
            i += 2
        }

        val node1 = n.getNode(1)
        val node2 = n.getNode(2)

        node1.array.size shouldBeExactly 4
        node2.array.size shouldBeExactly 4
    }

    @Suppress("UNCHECKED_CAST")
    "array property" {
        val shift = 0
        val isMutable = atomic(true)
        val leafFlag = Box(null)
        var i = 0
        var n: Node<String, Int> = BitMapIndexedNode()
        while (i < 20) {
            val key = "$i"
            n = n.assoc(isMutable, shift, hasheq(key), key, i, leafFlag)
            i += 2
        }

        n.array.size shouldBeExactly 14
        n.array[0] shouldBe "14"
        n.array[1] shouldBe 14
        n.array[8] shouldBe "16"
        n.array[9] shouldBe 16
        n.array[13] as BitMapIndexedNode<String, Int>
    }

    "singleKV()" {
        val shift = 0
        val isMutable = atomic(true)
        val leafFlag = Box(null)
        val n: Node<String, Int> = BitMapIndexedNode()
        val m = n.assoc(isMutable, shift, hasheq("a"), "a", 12, leafFlag)
        val o = m.assoc(isMutable, shift, hasheq("b"), "b", 18, leafFlag)

        n.isSingleKV().shouldBeFalse()
        o.isSingleKV().shouldBeFalse()

        m.isSingleKV().shouldBeTrue()
    }

    "without()" - {
        "when key doesn't exist, it should return this" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            val removedLeaf = Box(null)
            var i = 0
            val key = "30"
            var n: Node<String, Int> = BitMapIndexedNode()
            while (i < 20) {
                val k = "$i"
                n = n.assoc(isMutable, shift, hasheq(k), k, i, leafFlag)
                i += 2
            }

            val newNode: Node<String, Int> = n.without(
                isMutable, shift, hasheq(key), key, removedLeaf
            )

            newNode shouldBeSameInstanceAs n
            removedLeaf.value.shouldBeNull()
        }

        "when hash exists in the data half of the array" - {
            "when key isn't equiv with key in the array, return this" {
                val shift = 0
                val key = "J2RCvlt3yJ"
                val delKey = "RwtM1oQGxE"
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val removedLeaf = Box(null)
                val n = BitMapIndexedNode<String, Int>()
                    .assoc(isMutable, shift, hasheq(key), key, 15, leafFlag)

                val newNode = n.without(
                    isMutable, shift, hasheq(delKey), delKey, removedLeaf
                )

                newNode shouldBeSameInstanceAs n
                removedLeaf.value.shouldBeNull()
            }

            "when keys are equiv, it should remove key/value form array" - {
                "using copyAndRemove()" {
                    val shift = 0
                    val isMutable = atomic(true)
                    val leafFlag = Box(null)
                    val removedLeaf = Box(null)
                    val key = "6"
                    val hash = hasheq(key)
                    var n = BitMapIndexedNode<String, Int>()

                    var i = 0
                    while (i < 20) {
                        val k = "$i"
                        n = n.assoc(
                            isMutable, shift, hasheq(k), k, i, leafFlag
                        ) as BitMapIndexedNode<String, Int>
                        i += 2
                    }

                    val newNode = n.without(
                        isMutable, shift, hash, key, removedLeaf
                    ) as BitMapIndexedNode<String, Int>

                    newNode.array.size shouldBeExactly n.array.size - 2
                    removedLeaf.value shouldBeSameInstanceAs removedLeaf
                    newNode.datamap shouldBeExactly
                        (n.datamap xor LeanMap.bitpos(hash, shift))
                }

                "when shift == 0 and array size is 4" {
                    val shift = 0
                    val isMutable = atomic(true)
                    val leafFlag = Box(null)
                    val removedLeaf1 = Box(null)
                    val removedLeaf2 = Box(null)
                    var n = BitMapIndexedNode<String, Int>()
                    val key1 = "0"
                    val key2 = "2"
                    val keyHash1 = hasheq(key1)
                    val keyHash2 = hasheq(key2)

                    var i = 0
                    while (i < 4) {
                        val k = "$i"
                        n = n.assoc(
                            isMutable, shift, hasheq(k), k, i, leafFlag
                        ) as BitMapIndexedNode<String, Int>
                        i += 2
                    }

                    val newNode1 = n.without(
                        isMutable, shift, keyHash1, key1, removedLeaf1
                    ) as BitMapIndexedNode<String, Int>

                    val newNode2 = n.without(
                        isMutable, shift, keyHash2, key2, removedLeaf2
                    ) as BitMapIndexedNode<String, Int>

                    newNode1.array.size shouldBeExactly 2
                    removedLeaf1.value shouldBeSameInstanceAs removedLeaf1
                    newNode1.nodemap shouldBeExactly 0
                    newNode1.datamap shouldBeExactly
                        (n.datamap xor LeanMap.bitpos(keyHash1, shift))
                    newNode1.isMutable shouldBeSameInstanceAs isMutable

                    newNode2.array.size shouldBeExactly 2
                    removedLeaf2.value shouldBeSameInstanceAs removedLeaf2
                    newNode2.nodemap shouldBeExactly 0
                    newNode2.datamap shouldBeExactly
                        LeanMap.bitpos(keyHash1, shift)
                    newNode2.isMutable shouldBeSameInstanceAs isMutable
                }
            }
        }

        "when hash exists in nodes half of the array" - {
            """when key exists and and 1 pair left in subNode after removal,
                   it should push it up to datamap
                """ {
                val shift = 0
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val removedLeaf = Box(null)
                var n = BitMapIndexedNode<String, Int>()
                val key = "18"
                val keyHash = hasheq(key)
                val bitpos = LeanMap.bitpos(keyHash, shift)

                var i = 0
                while (i < 20) {
                    val k = "$i"
                    n = n.assoc(
                        isMutable, shift, hasheq(k), k, i, leafFlag
                    ) as BitMapIndexedNode<String, Int>
                    i += 2
                }

                val newNode = n.without(
                    isMutable, shift, keyHash, key, removedLeaf
                ) as BitMapIndexedNode<String, Int>

                newNode.array[2] shouldBe "12"
                newNode.array[3] shouldBe 12
                newNode.datamap shouldBeExactly (n.datamap or bitpos)
                newNode.nodemap shouldBeExactly (n.nodemap xor bitpos)
                removedLeaf.value.shouldNotBeNull()
            }

            @Suppress("UNCHECKED_CAST")
            """when key exists and and more than 1 pair left in newSubNode
                   after removal, it should update the array using newSubNode
                """ {
                val shift = 0
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val removedLeaf = Box(null)
                var n = BitMapIndexedNode<String, Int>()
                val key = "96"
                val keyHash = hasheq(key)

                var i = 0
                while (i < 100) {
                    val k = "$i"
                    n = n.assoc(
                        isMutable, shift, hasheq(k), k, i, leafFlag
                    ) as BitMapIndexedNode<String, Int>
                    i += 2
                }
                val subNode = n.array[35] as BitMapIndexedNode<String, Int>

                val newNode = n.without(
                    isMutable, shift, keyHash, key, removedLeaf
                ) as BitMapIndexedNode<String, Int>
                val newSubNode = newNode.array[35]
                    as BitMapIndexedNode<String, Int>

                newNode shouldBeSameInstanceAs n
                newNode.datamap shouldBeExactly newNode.datamap
                newNode.nodemap shouldBeExactly newNode.nodemap
                newSubNode.array.size shouldBeExactly subNode.array.size - 2
            }

            @Suppress("UNCHECKED_CAST")
            """when key exists and there is only 1 node in the array and
                   1 pair left in subNode after removal, return the subNode""" {
                val shift = 0
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val removedLeaf = Box(null)
                var n = BitMapIndexedNode<String, Int>()
                val key = "1958"
                val keyHash = hasheq(key)
                var i = 0
                while (i < 2000) {
                    val k = "$i"
                    n = n.assoc(
                        isMutable, shift, hasheq(k), k, i, leafFlag
                    ) as BitMapIndexedNode<String, Int>
                    i += 2
                }

                val newNode = n.without(
                    isMutable, shift, keyHash, key, removedLeaf
                ) as BitMapIndexedNode<String, Int>

                val newSubNode = newNode.array[10]
                    as BitMapIndexedNode<String, Int>
                newSubNode.array[2] shouldBe "642"
                newSubNode.array[3] shouldBe 642
                newNode.nodemap shouldBeExactly -1
                newNode.datamap shouldBeExactly 0
            }

            "when key doesn't exist, return this" {
                val shift = 0
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                val removedLeaf = Box(null)
                var n = BitMapIndexedNode<String, Int>()
                val key = "4000"
                val keyHash = hasheq(key)
                var i = 0
                while (i < 2000) {
                    val k = "$i"
                    n = n.assoc(
                        isMutable, shift, hasheq(k), k, i, leafFlag
                    ) as BitMapIndexedNode<String, Int>
                    i += 2
                }

                val newNode = n.without(
                    isMutable, shift, keyHash, key, removedLeaf
                ) as BitMapIndexedNode<String, Int>

                newNode shouldBeSameInstanceAs n
            }
        }
    }

    "find(...key, default) should return value" - {
        "when key doesn't exist return default" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            var n = BitMapIndexedNode<String, Int>()
            var i = 0
            while (i < 20) {
                val k = "$i"
                n = n.assoc(
                    isMutable, shift, hasheq(k), k, i, leafFlag
                ) as BitMapIndexedNode<String, Int>
                i += 2
            }
            val default = -1

            n.find(shift, hasheq("80"), "80", default) shouldBe default
            n.find(shift, hasheq("28"), "28", default) shouldBe default
        }

        "when exists, it should return the associated value" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            var n = BitMapIndexedNode<String, Int>()
            var i = 0
            while (i < 20) {
                val k = "$i"
                n = n.assoc(
                    isMutable, shift, hasheq(k), k, i, leafFlag
                ) as BitMapIndexedNode<String, Int>
                i += 2
            }

            n.find(shift, hasheq("6"), "6", -1) shouldBe 6
            n.find(shift, hasheq("18"), "18", -1) shouldBe 18
        }

        "compare keys with equiv()" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            val k = 1L
            val n = BitMapIndexedNode<Any, String>().assoc(
                isMutable, shift, hasheq(k), k, "1L", leafFlag
            ) as BitMapIndexedNode<Any, String>

            n.find(shift, hasheq(1), 1, "notFound") shouldBe "1L"
        }
    }

    "find(...key) should return IMapEntry" - {
        "when key doesn't exist return null" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            var n = BitMapIndexedNode<String, Int>()
            var i = 0
            while (i < 20) {
                val k = "$i"
                n = n.assoc(
                    isMutable, shift, hasheq(k), k, i, leafFlag
                ) as BitMapIndexedNode<String, Int>
                i += 2
            }
            n.find(shift, hasheq("80"), "80").shouldBeNull()
            n.find(shift, hasheq("28"), "28").shouldBeNull()
        }

        "when exists, it should return IMapEntry of key/value" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            var n = BitMapIndexedNode<String, Int>()
            var i = 0
            while (i < 20) {
                val k = "$i"
                n = n.assoc(
                    isMutable, shift, hasheq(k), k, i, leafFlag
                ) as BitMapIndexedNode<String, Int>
                i += 2
            }

            n.find(shift, hasheq("6"), "6") shouldBe
                MapEntry("6", 6)
            n.find(shift, hasheq("18"), "18") shouldBe
                MapEntry("18", 18)
        }
    }

    "nodeSeq()" - {
        "when datamap > 0" {
            val shift = 0
            val isMutable = atomic(true)
            val leafFlag = Box(null)
            var n = BitMapIndexedNode<String, Int>()
            var i = 0
            while (i < 20) {
                val k = "$i"
                n = n.assoc(
                    isMutable, shift, hasheq(k), k, i, leafFlag
                ) as BitMapIndexedNode<String, Int>
                i += 2
            }

            val nodeSeq = n.nodeSeq() as LeanMap.NodeSeq<String, Int>

            nodeSeq.array shouldBeSameInstanceAs n.array
            nodeSeq.lvl shouldBeExactly 0
            nodeSeq.nodes.size shouldBeExactly 7
            nodeSeq.nodes[0] shouldBeSameInstanceAs n
            nodeSeq.cursorLengths.size shouldBeExactly 7
            nodeSeq.cursorLengths[0] shouldBeExactly n.nodeArity()
            nodeSeq.dataIndex shouldBeExactly 0
            nodeSeq.dataLength shouldBeExactly n.dataArity() - 1

            nodeSeq.first() shouldBe MapEntry("14", 14)
        }

        "when datamap == 0" - {
            "when node is empty, it should return an empty seq" {
                val node = BitMapIndexedNode<String, Int>()

                node.nodeSeq() shouldBeSameInstanceAs PersistentList.Empty
            }

            "it should return a seq of map entries" {
                val shift = 0
                val isMutable = atomic(true)
                val leafFlag = Box(null)
                var n = BitMapIndexedNode<String, Int>()
                var i = 0
                while (i < 1000) {
                    val k = "$i"
                    n = n.assoc(
                        isMutable, shift, hasheq(k), k, i, leafFlag
                    ) as BitMapIndexedNode<String, Int>
                    i += 2
                }

                val nodeSeq = n.nodeSeq() as LeanMap.NodeSeq<String, Int>

                nodeSeq.count shouldBeExactly 500
                nodeSeq[0] shouldBe MapEntry("672", 672)
                nodeSeq[499] shouldBe MapEntry("784", 784)
            }
        }
    }
})
