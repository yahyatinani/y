package io.github.yahyatinani.y.core.collections.map.hashmap

import io.github.yahyatinani.y.core.collections.Edit
import io.github.yahyatinani.y.core.collections.MapEntry
import io.github.yahyatinani.y.core.collections.PersistentHashMap.BitMapIndexedNode
import io.github.yahyatinani.y.core.collections.PersistentHashMap.BitMapIndexedNode.EmptyBitMapIndexedNode
import io.github.yahyatinani.y.core.collections.PersistentHashMap.HashCollisionNode
import io.github.yahyatinani.y.core.util.Box
import io.github.yahyatinani.y.core.util.hasheq
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

class HashCollisionNodeTest : FreeSpec({
  "singleKV() when 1 pair in array, it should return true, otherwise false" {
    val edit = Edit(Any())
    val a1: Array<Any?> = arrayOf("a", 1)
    val a2: Array<Any?> = arrayOf()
    val hash = hasheq("a")
    val hcNode1 = HashCollisionNode<String, Int>(edit, hash, 1, a1)
    val hcNode2 = HashCollisionNode<String, Int>(edit, hash, 0, a2)

    hcNode1.isSingleKV().shouldBeTrue()
    hcNode2.isSingleKV().shouldBeFalse()
  }

  "hasNodes() should return false" {
    val edit = Edit(Any())
    val a: Array<Any?> = arrayOf("a", 1)
    val hash = hasheq("a")
    val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)

    hcNode.hasNodes().shouldBeFalse()
  }

  "hasData() should return true" {
    val edit = Edit(Any())
    val a: Array<Any?> = arrayOf("a", 1)
    val hash = hasheq("a")
    val hcNode =
      HashCollisionNode<String, Int>(edit, hash, 1, a)

    hcNode.hasData().shouldBeTrue()
  }

  "nodeArity() should return 0" {
    val edit = Edit(Any())
    val a: Array<Any?> = arrayOf("a", 1)
    val hash = hasheq("a")
    val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)

    hcNode.nodeArity() shouldBeExactly 0
  }

  "dataArity() should return count" {
    val edit = Edit(Any())
    val a1: Array<Any?> = arrayOf("a", 1)
    val a2: Array<Any?> = arrayOf("a", 1, "b", 2)
    val hash = hasheq("a")
    val hcNode1 = HashCollisionNode<String, Int>(edit, hash, 1, a1)
    val hcNode2 = HashCollisionNode<String, Int>(edit, hash, 2, a2)

    hcNode1.dataArity() shouldBeExactly hcNode1.count
    hcNode2.dataArity() shouldBeExactly hcNode2.count
  }

  "getNode() should throw UnsupportedOperationException" {
    val edit = Edit(Any())
    val a: Array<Any?> = arrayOf("a", 1)
    val hash = hasheq("a")
    val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)

    shouldThrowExactly<UnsupportedOperationException> {
      hcNode.getNode(0)
    }.message shouldBe "HashCollisionNode has no nodes!"
  }

  "array property" {
    val edit = Edit(Any())
    val a: Array<Any?> = arrayOf("a", 1)
    val hash = hasheq("a")
    val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)

    hcNode.array shouldBeSameInstanceAs a
  }

  "nodeSeq() should throw UnsupportedOperationException" {
    val edit = Edit(Any())
    val a: Array<Any?> = arrayOf("a", 1)
    val hash = hasheq("a")
    val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)

    shouldThrowExactly<UnsupportedOperationException> {
      hcNode.nodeSeq()
    }.message shouldBe "HashCollisionNode has no nodes!"
  }

  "find(...key, default) should return value" - {
    "when key doesn't exist return default" {
      val edit = Edit(Any())
      val a: Array<Any?> = arrayOf("a", 1)
      val hash = hasheq("a")
      val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)
      val default = -1

      hcNode.find(0, hash, "b", default) shouldBe default
    }

    "when exists, it should return the associated value" {
      val edit = Edit(Any())
      val a1: Array<Any?> = arrayOf("a", 1)
      val a2: Array<Any?> = arrayOf(1, "1", 2L, "2L")
      val hash1 = hasheq("a")
      val hash2 = hasheq("2")
      val hcNode1 = HashCollisionNode<String, Int>(edit, hash1, 1, a1)
      val hcNode2 = HashCollisionNode<Int, String>(edit, hash2, 2, a2)
      val default = -1

      hcNode1.find(0, hash1, "a", default) shouldBe 1
      hcNode2.find(0, hash2, 2, "not_found") shouldBe "2L"
    }
  }

  "find(...key) should return IMapEntry" - {
    "when key doesn't exist return default" {
      val edit = Edit(Any())
      val a: Array<Any?> = arrayOf("a", 1)
      val hash = hasheq("a")
      val hcNode = HashCollisionNode<String, Int>(edit, hash, 1, a)

      hcNode.find(0, hash, "b").shouldBeNull()
    }

    "when exists, it should return the associated value" {
      val edit = Edit(Any())
      val a1: Array<Any?> = arrayOf("a", 1)
      val a2: Array<Any?> = arrayOf(1, "1", 2L, "2L")
      val hash1 = hasheq("a")
      val hash2 = hasheq("2")
      val hcNode1 = HashCollisionNode<String, Int>(edit, hash1, 1, a1)
      val hcNode2 = HashCollisionNode<Int, String>(edit, hash2, 2, a2)

      hcNode1.find(0, hash1, "a") shouldBe MapEntry("a", 1)
      hcNode2.find(0, hash2, 2) shouldBe MapEntry(2L, "2L")
    }
  }

  "assoc()" - {
    "when it is allowed to mutate this HashCollisionNode" - {
      """when key doesn't exist, it should add the new pair by mutating
               this array""" {
        val edit = Edit(Any())
        val a: Array<Any?> = arrayOf("a", 1)
        val leafFlag = Box(null)
        val hcNode =
          HashCollisionNode<String, Int>(edit, hasheq("a"), 1, a)

        val newHcNode =
          hcNode.assoc(edit, 0, hasheq("b"), "b", 2, leafFlag)
            as HashCollisionNode<String, Int>

        newHcNode shouldBeSameInstanceAs hcNode
        leafFlag.value shouldBeSameInstanceAs leafFlag
        newHcNode.count shouldBeExactly 2
        newHcNode.array.size shouldBeExactly 4
        newHcNode.array shouldContain "b"
        newHcNode.array shouldContain 2
      }

      """when key already exist, it should update the value associated
               with it if they're different""" {
        val edit = Edit(Any())
        val a: Array<Any?> = arrayOf("a", 1)
        val addedLeaf = Box(null)
        val hcNode =
          HashCollisionNode<String, Int>(edit, hasheq("a"), 1, a)

        val newHcNode =
          hcNode.assoc(edit, 0, hasheq("a"), "a", 7, addedLeaf)
            as HashCollisionNode<String, Int>

        newHcNode shouldBeSameInstanceAs hcNode
        addedLeaf.value.shouldBeNull()
        newHcNode.count shouldBeExactly hcNode.count
        newHcNode.array shouldBeSameInstanceAs hcNode.array
        newHcNode.array.size shouldBeExactly 2
        newHcNode.array shouldContain "a"
        newHcNode.array shouldContain 7
      }
    }

    "when it is NOT allowed to mutate this HashCollisionNode" - {
      """when key doesn't exist, it should add the new pair and return a
                new HashCollisionNode""" {
        val edit = Edit(null)
        val a: Array<Any?> = arrayOf("a", 1)
        val leafFlag = Box(null)
        val hcNode =
          HashCollisionNode<String, Int>(edit, hasheq("a"), 1, a)

        val newHcNode =
          hcNode.assoc(Edit(Any()), 0, hasheq("b"), "b", 2, leafFlag)
            as HashCollisionNode<String, Int>

        hcNode.count shouldBeExactly 1
        hcNode.array.size shouldBeExactly 2
        hcNode.array[0] shouldBe "a"
        hcNode.array[1] shouldBe 1

        leafFlag.value shouldBeSameInstanceAs leafFlag
        newHcNode shouldNotBeSameInstanceAs hcNode
        newHcNode.edit shouldBeSameInstanceAs hcNode.edit
        newHcNode.hash shouldBeExactly hcNode.hash
        newHcNode.count shouldBeExactly 2
        newHcNode.array.size shouldBeExactly 4
        newHcNode.array shouldContain "b"
        newHcNode.array shouldContain 2
      }

      """when key already exist but values are different, it should
               return a new node with updated value""" {
        val edit = Edit(null)
        val a: Array<Any?> = arrayOf("a", 1)
        val leafFlag = Box(null)
        val hcNode =
          HashCollisionNode<String, Int>(edit, hasheq("a"), 1, a)

        val newHcNode =
          hcNode.assoc(Edit(Any()), 0, hasheq("a"), "a", 7, leafFlag)
            as HashCollisionNode<String, Int>

        hcNode.count shouldBeExactly 1
        hcNode.array.size shouldBeExactly 2
        hcNode.array[0] shouldBe "a"
        hcNode.array[1] shouldBe 1

        leafFlag.value.shouldBeNull()
        newHcNode shouldNotBeSameInstanceAs hcNode
        newHcNode.edit shouldBeSameInstanceAs hcNode.edit
        newHcNode.hash shouldBeExactly hcNode.hash
        newHcNode.count shouldBeExactly 1
        newHcNode.array.size shouldBeExactly 2
        newHcNode.array shouldContain "a"
        newHcNode.array shouldContain 7
      }

      """when key already exist AND values are equal, it should
               return this without any updates""" {
        val edit = Edit(null)
        val a: Array<Any?> = arrayOf("a", 1)
        val leafFlag = Box(null)
        val hcNode =
          HashCollisionNode<String, Int>(edit, hasheq("a"), 1, a)

        val newHcNode =
          hcNode.assoc(Edit(Any()), 0, hasheq("a"), "a", 1, leafFlag)
            as HashCollisionNode<String, Int>

        hcNode.count shouldBeExactly 1
        hcNode.array.size shouldBeExactly 2
        hcNode.array[0] shouldBe "a"
        hcNode.array[1] shouldBe 1

        newHcNode shouldBeSameInstanceAs hcNode
        leafFlag.value.shouldBeNull()
      }
    }
  }

  "without()" - {
    "when key doesn't exist, it should return this as it is" {
      val edit = Edit(null)
      val a: Array<Any?> = arrayOf("a", 1)
      val removedLeaf = Box(null)
      val hcNode =
        HashCollisionNode<String, Int>(edit, hasheq("a"), 1, a)

      val newHcNode =
        hcNode.without(edit, 0, hasheq("b"), "b", removedLeaf)
          as HashCollisionNode<String, Int>

      newHcNode shouldBeSameInstanceAs hcNode
    }

    "when key does exist" - {
      "when count == 1, it should return the EmptyBitmapIndexedNode" {
        val edit = Edit(null)
        val a: Array<Any?> = arrayOf("a", 1)
        val removedLeaf = Box(null)
        val key = "a"
        val keyHash = hasheq(key)
        val hcNode =
          HashCollisionNode<String, Int>(edit, keyHash, 1, a)

        val newHcNode =
          hcNode.without(edit, 0, keyHash, key, removedLeaf)

        newHcNode shouldBeSameInstanceAs EmptyBitMapIndexedNode
        removedLeaf.value shouldBeSameInstanceAs removedLeaf
      }

      "when count == 2, return the one pair left as a BitmapIndexedNode" {
        val edit = Edit(null)
        val a: Array<Any?> = arrayOf("a", 1, "b", 2)
        val removedLeaf = Box(null)
        val key = "a"
        val keyHash = hasheq(key)
        val hcNode =
          HashCollisionNode<String, Int>(edit, keyHash, 2, a)

        val newHcNode1 =
          hcNode.without(edit, 0, keyHash, key, removedLeaf)
            as BitMapIndexedNode<String, Int>

        val newHcNode2 =
          hcNode.without(edit, 0, hasheq("b"), "b", Box(null))
            as BitMapIndexedNode<String, Int>

        newHcNode1.edit shouldBeSameInstanceAs edit
        newHcNode1.nodemap shouldBeExactly 0
        newHcNode1.datamap shouldBeExactly 131072
        newHcNode1.array.size shouldBeExactly 2
        newHcNode1.array[0] shouldBe "b"
        newHcNode1.array[1] shouldBe 2

        newHcNode2.array[0] shouldBe "a"
        newHcNode2.array[1] shouldBe 1
      }

      "when count > 2, return HashCollisionNode without the pair" {
        val edit = Edit(null)
        val a: Array<Any?> = arrayOf("a", 1, "b", 2, "c", 3)
        val removedLeaf = Box(null)
        val key = "a"
        val keyHash = hasheq(key)
        val hcNode =
          HashCollisionNode<String, Int>(edit, keyHash, 3, a)

        val newHcNode =
          hcNode.without(edit, 0, keyHash, key, removedLeaf)
            as HashCollisionNode<String, Int>

        hcNode.count shouldBeExactly 3
        hcNode.array.size shouldBeExactly 6

        newHcNode.count shouldBeExactly 2
        newHcNode.array.size shouldBeExactly 4
        newHcNode.array[0] shouldBe "b"
        newHcNode.array[1] shouldBe 2
        newHcNode.array[2] shouldBe "c"
        newHcNode.array[3] shouldBe 3
      }
    }
  }
})
