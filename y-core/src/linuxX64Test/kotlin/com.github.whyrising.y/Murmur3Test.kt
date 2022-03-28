package com.github.whyrising.y

import com.github.whyrising.y.util.Murmur3
import io.kotest.matchers.ints.shouldBeExactly
import kotlin.test.Test

class Murmur3Test {
    @Test
    fun hashOrderedTest() {
        val l1 = l("Mango", 1, 32569885145, 12.toShort(), -0.0f, true)

        Murmur3.hashOrdered(l1) shouldBeExactly -40009531
    }
}
