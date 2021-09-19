package com.github.whyrising.y.collections.seq

import com.github.whyrising.y.collections.core.lazySeq
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LazySeqTest {
    @Test
    fun `empty lazySeq()`() {
        val seq = lazySeq<Int>()

        seq.count shouldBeExactly 0
        seq.toString() shouldBe "()"
    }
}
