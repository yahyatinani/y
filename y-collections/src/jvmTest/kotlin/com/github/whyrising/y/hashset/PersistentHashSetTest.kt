package com.github.whyrising.y.hashset

import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.PersistentHashSet.EmptyHashSet
import com.github.whyrising.y.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.atomicfu.atomic

class PersistentHashSetTest : FreeSpec({
    "EmptyHashSet" - {
        "map property should be set to EmptyLeanMap" {
            EmptyHashSet.map shouldBeSameInstanceAs EmptyLeanMap
        }
    }

    "TransientHashSet" - {
        "count should return the count of the inner transient map" {
            val tmap1 = atomic(m<Int, Int>().asTransient())
            val tmap2 = atomic(m("a" to "1").asTransient())

            val tSet1 = TransientHashSet(tmap1)
            val tSet2: TransientHashSet<String> = TransientHashSet(tmap2)

            tSet1.count shouldBeExactly 0
            tSet2.count shouldBeExactly tmap2.value.count
        }
    }
})
