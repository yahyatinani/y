package com.github.whyrising.y.hashset

import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.PersistentHashSet.EmptyHashSet
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs

class PersistentHashSetTest : FreeSpec({
    "EmptyHashSet" - {
        "map property should be set to EmptyLeanMap" {
            EmptyHashSet.map shouldBeSameInstanceAs EmptyLeanMap
        }
    }
})
