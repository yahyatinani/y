package com.github.whyrising.y.map

import com.github.whyrising.y.concretions.map.get
import com.github.whyrising.y.concretions.map.m
import com.github.whyrising.y.concretions.vector.v
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CoreTest {
    @Test
    fun `get(map, key)`() {
        get(m(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
        get(mapOf(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
        get(v(1, 2, 3, 4), 0) shouldBe 1
    }
}
