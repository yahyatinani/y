package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.core.l
import com.github.whyrising.y.collections.core.v
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
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

    @Test
    fun `peek() should return null, when queue is empty`() {
        PersistentQueue<Int>().peek().shouldBeNull()
    }

    @Test
    fun `peek() should return the first element in the queue`() {
        val queue = PersistentQueue<Int>()
            .conj(45)
            .conj(90)
            .conj(100)

        queue.peek()!! shouldBeExactly 45
    }

    @Test
    fun `pop() should return the empty queue when called on empty queue`() {
        val queue = PersistentQueue<Int>()

        queue.pop() shouldBeSameInstanceAs queue
    }

    /**
     * pop() should drop the first element from the front of the queue and then
     * put the back in front of the queue, when the front becomes empty
     */
    @Test
    fun `pop() drops first element from front & put the back in front`() {
        val queue = PersistentQueue<Int>()
            .conj(45)
            .conj(90)
            .conj(100)

        val newQueue = queue.pop()

        newQueue.count shouldBeExactly 2
        newQueue.front shouldBe l(90, 100)
        newQueue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
    }

    @Test
    fun `pop() should drop the first element from the front of the queue`() {
        val queue = PersistentQueue<Int>()
            .conj(45)
            .conj(90)
            .conj(100)
            .pop()
            .conj(111)
            .conj(222)

        val newQueue = queue.pop()

        newQueue.count shouldBeExactly 3
        newQueue.front shouldBe l(100)
        newQueue.back shouldBe v(111, 222)
    }

    @Test
    fun `empty() should return the empty queue`() {
        PersistentQueue<Int>().empty() shouldBeSameInstanceAs
            PersistentQueue<Int>()
    }
}
