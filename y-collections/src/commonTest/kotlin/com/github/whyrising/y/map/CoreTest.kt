package com.github.whyrising.y.map

import com.github.whyrising.y.concretions.map.PersistentArrayMap
import com.github.whyrising.y.concretions.map.get
import com.github.whyrising.y.concretions.map.m
import com.github.whyrising.y.concretions.set.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.concretions.set.hashSet
import com.github.whyrising.y.concretions.vector.v
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CoreTest {
    @Test
    fun `get(map, key)`() {
        val transientHashSet = TransientHashSet(
            (m(
                ":a" to 5,
                ":b" to 6,
                ":c" to 3
            ) as PersistentArrayMap).asTransient()
        )

        get(mapOf(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
        get(m(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
        get(v(5, 6, 9, 3), 0) shouldBe 5
        get(hashSet(54, 69, 36), 54) shouldBe 54
        get(transientHashSet, ":a") shouldBe 5
    }
}
