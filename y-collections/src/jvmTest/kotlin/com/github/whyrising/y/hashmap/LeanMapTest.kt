package com.github.whyrising.y.hashmap

import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.Companion.bitpos
import com.github.whyrising.y.LeanMap.TransientLeanMap
import com.github.whyrising.y.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull

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

    @Suppress("UNCHECKED_CAST")
    "asTransient()" {
        val leanMap = LeanMap<String, Int>()

        val t = leanMap.asTransient() as TransientLeanMap<String, Int>

        t.count shouldBeExactly 0
        t.root.value.shouldBeNull()
    }

    "invoke(...pairs)" {
        LeanMap("a" to 1).count shouldBeExactly 1
        LeanMap("a" to 1, "b" to 2).count shouldBeExactly 2
        LeanMap("a" to 1, "b" to 2, "c" to 3).count shouldBeExactly 3
    }
})
