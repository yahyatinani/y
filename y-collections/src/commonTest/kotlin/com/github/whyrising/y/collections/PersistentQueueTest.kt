package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class PersistentQueueTest {
    @Test
    fun `empty PersistentQueue`() {
        val queue = PersistentQueue<Int>()

        queue.count shouldBeExactly 0
        queue.front shouldBeSameInstanceAs PersistentList.Empty
        queue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
    }
}
