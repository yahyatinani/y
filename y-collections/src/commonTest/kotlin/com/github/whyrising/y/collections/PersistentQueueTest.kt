package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.core.l
import com.github.whyrising.y.collections.core.v
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class PersistentQueueTest {
    @Test
    fun `empty PersistentQueue`() {
        val queue = PersistentQueue<Int>()

        queue.count shouldBeExactly 0
        queue.front shouldBeSameInstanceAs PersistentList.Empty
        queue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
        queue shouldBeSameInstanceAs PersistentQueue<Int>()
    }

    @Test
    fun `conj() should add an element to the front, when queue is empty`() {
        val queue = PersistentQueue<Int>()

        val newQueue = queue.conj(45)

        newQueue.count shouldBeExactly 1
        newQueue.front shouldBe l(45)
        newQueue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
    }

    @Test
    fun `conj() should add an element to the back, when queue is non empty`() {
        val queue = PersistentQueue<Int>().conj(45)

        val newQueue = queue
            .conj(90)
            .conj(100)
            .conj(200)

        newQueue.count shouldBeExactly 4
        newQueue.front shouldBe l(45)
        newQueue.back shouldBe v(90, 100, 200)
    }
}
