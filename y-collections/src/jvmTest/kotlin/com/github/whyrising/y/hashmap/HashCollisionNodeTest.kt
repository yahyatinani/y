package com.github.whyrising.y.hashmap

import com.github.whyrising.y.LeanMap.HashCollisionNode
import com.github.whyrising.y.hasheq
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import kotlinx.atomicfu.atomic

@ExperimentalStdlibApi
class HashCollisionNodeTest : FreeSpec({
    "singleKV() when 1 pair in array, it should return true, otherwise false" {
        val mutable = atomic(true)
        val a1: Array<Any?> = arrayOf("a", 1)
        val a2: Array<Any?> = arrayOf()
        val hash = hasheq("a")
        val hcNode1 = HashCollisionNode<String, Int>(mutable, hash, 1, a1)
        val hcNode2 = HashCollisionNode<String, Int>(mutable, hash, 0, a2)

        hcNode1.isSingleKV().shouldBeTrue()
        hcNode2.isSingleKV().shouldBeFalse()
    }

    "hasNodes() should return false" {
        val mutable = atomic(true)
        val a: Array<Any?> = arrayOf("a", 1)
        val hash = hasheq("a")
        val hcNode = HashCollisionNode<String, Int>(mutable, hash, 1, a)

        hcNode.hasNodes().shouldBeFalse()
    }

    "hasData() should return true" {
        val mutable = atomic(true)
        val a: Array<Any?> = arrayOf("a", 1)
        val hash = hasheq("a")
        val hcNode = HashCollisionNode<String, Int>(mutable, hash, 1, a)

        hcNode.hasData().shouldBeTrue()
    }

    "nodeArity() should return 0" {
        val mutable = atomic(true)
        val a: Array<Any?> = arrayOf("a", 1)
        val hash = hasheq("a")
        val hcNode = HashCollisionNode<String, Int>(mutable, hash, 1, a)

        hcNode.nodeArity() shouldBeExactly 0
    }

    "dataArity() should return count" {
        val mutable = atomic(true)
        val a1: Array<Any?> = arrayOf("a", 1)
        val a2: Array<Any?> = arrayOf("a", 1, "b", 2)
        val hash = hasheq("a")
        val hcNode1 = HashCollisionNode<String, Int>(mutable, hash, 1, a1)
        val hcNode2 = HashCollisionNode<String, Int>(mutable, hash, 2, a2)

        hcNode1.dataArity() shouldBeExactly hcNode1.count
        hcNode2.dataArity() shouldBeExactly hcNode2.count
    }

    "getNode() should throw UnsupportedOperationException" {
        val mutable = atomic(true)
        val a: Array<Any?> = arrayOf("a", 1)
        val hash = hasheq("a")
        val hcNode = HashCollisionNode<String, Int>(mutable, hash, 1, a)

        shouldThrowExactly<UnsupportedOperationException> {
            hcNode.getNode(0)
        }
    }
})
