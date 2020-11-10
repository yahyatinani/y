package com.github.whyrising.y.hashmap

import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.Companion.bitpos
import com.github.whyrising.y.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly

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
})
