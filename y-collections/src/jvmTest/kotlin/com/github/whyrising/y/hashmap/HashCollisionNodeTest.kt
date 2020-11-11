package com.github.whyrising.y.hashmap

import com.github.whyrising.y.LeanMap.HashCollisionNode
import com.github.whyrising.y.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.atomicfu.atomic

@ExperimentalStdlibApi
class HashCollisionNodeTest : FreeSpec({
    "singleKV()" {
        val mutable = atomic(true)
        val a1: Array<Any?> = arrayOf("a", 1)
        val a2: Array<Any?> = arrayOf()
        val hash = hasheq("a")
        val hcNode1 = HashCollisionNode<String, Int>(mutable, hash, 1, a1)
        val hcNode2 = HashCollisionNode<String, Int>(mutable, hash, 0, a2)

        hcNode1.isSingleKV().shouldBeTrue()
        hcNode2.isSingleKV().shouldBeFalse()
    }

    "hasNodes" {
        val mutable = atomic(true)
        val a: Array<Any?> = arrayOf("a", 1)
        val hash = hasheq("a")
        val hcNode = HashCollisionNode<String, Int>(mutable, hash, 1, a)

        hcNode.hasNodes().shouldBeFalse()
    }
})
