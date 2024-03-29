package io.github.yahyatinani.y.core.collections

import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.collections.PersistentQueue.Seq
import io.github.yahyatinani.y.core.l
import io.github.yahyatinani.y.core.q
import io.github.yahyatinani.y.core.v
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class PersistentQueueTest : FreeSpec({
  "empty PersistentQueue" {
    val queue = PersistentQueue<Int>()

    queue.count shouldBeExactly 0
    queue.front shouldBeSameInstanceAs Empty
    queue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
    queue shouldBeSameInstanceAs PersistentQueue<Int>()
  }

  "conj() should add an element to the front, when queue is empty" {
    val queue = PersistentQueue<Int>()

    val newQueue = queue.conj(45)

    newQueue.count shouldBeExactly 1
    newQueue.front shouldBe l(45)
    newQueue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
  }

  "conj() should add an element to the back, when queue is non empty" {
    val queue = PersistentQueue<Int>().conj(45)

    val newQueue = queue
      .conj(90)
      .conj(100)
      .conj(200)

    newQueue.count shouldBeExactly 4
    newQueue.front shouldBe l(45)
    newQueue.back shouldBe v(90, 100, 200)
  }

  "peek() should return null, when queue is empty" {
    PersistentQueue<Int>().peek().shouldBeNull()
  }

  "peek() should return the first element in the queue" {
    val queue = PersistentQueue<Int>()
      .conj(45)
      .conj(90)
      .conj(100)

    queue.peek()!! shouldBeExactly 45
  }

  "pop() should return the empty queue when called on empty queue" {
    val queue = PersistentQueue<Int>()

    queue.pop() shouldBeSameInstanceAs queue
  }

  /**
   * pop() should drop the first element from the front of the queue and then
   * put the back in front of the queue, when the front becomes empty
   */
  "pop() drops first element from front & put the back in front" {
    val queue = PersistentQueue<Int>()
      .conj(45)
      .conj(90)
      .conj(100)

    val newQueue = queue.pop()

    newQueue.count shouldBeExactly 2
    newQueue.front shouldBe l(90, 100)
    newQueue.back shouldBeSameInstanceAs PersistentVector.EmptyVector
  }

  "pop() should drop the first element from the front of the queue" {
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

  "pop() should return the empty queue when queue has only one item" {
    val queue = PersistentQueue<Int>().conj(45)

    val newQueue = queue.pop()

    newQueue shouldBeSameInstanceAs PersistentQueue<Int>()
  }

  "empty() should return the empty queue" {
    PersistentQueue<Int>().empty() shouldBeSameInstanceAs
      PersistentQueue<Int>()
  }

  "PersistentQueue.Seq" - {
    "ctor" {
      val seq = Seq(l(1), v(2, 3, 4, 5).seq())

      seq.count shouldBeExactly 5
      seq.first() shouldBeExactly 1
      seq.next() shouldBe l(2, 3, 4, 5)
    }

    "next" {
      Seq(l(), v<Int>().seq()).next().shouldBeNull()
      Seq(l(1), v(2).seq()).next() shouldBe l(2)
      Seq(l(1, 2), v<Int>().seq()).next() shouldBe l(2)
      Seq(l(1, 2), v(3, 4).seq()).next() shouldBe l(2, 3, 4)
    }
  }

  "seq()" - {
    "seq() should return Empty seq when queue is empty" {
      PersistentQueue<Int>().seq() shouldBe Empty
    }

    "seq()" {
      val queue = PersistentQueue<Int>()
        .conj(45)
        .conj(90)
        .conj(100)

      queue.seq() shouldBe l(45, 90, 100)
    }
  }

  "hashCode assertions" {
    PersistentQueue<Int?>().conj(1).conj(2).conj(3).conj(null)
      .hashCode() shouldBeExactly 955327
    PersistentQueue<Int>().hashCode() shouldBeExactly 1
  }

  "hasheq" {
    val q = q(l(1))
    q._hasheq shouldBeExactly 0
    q._hash shouldBeExactly 0

    q.hasheq() shouldBeExactly -1381383523
    q.hashCode() shouldBeExactly 32

    q.hasheq() shouldBeExactly -1381383523
    q.hashCode() shouldBeExactly 32

    PersistentQueue<Int?>().conj(1).conj(2).conj(3).conj(null)
      .hasheq() shouldBeExactly 762779652
    PersistentQueue<Int>().hasheq() shouldBeExactly -2017569654
  }

  "equiv" {
    PersistentQueue<Any>().conj(1L).conj(2).equiv(v(1)).shouldBeFalse()
    PersistentQueue<Any>().conj(1L).conj(2).equiv(Any()).shouldBeFalse()
    PersistentQueue<Any>().conj(1L).equiv(l(2)).shouldBeFalse()
    (PersistentQueue<Any>().equiv(PersistentQueue<Any>().conj(v(1))))
      .shouldBeFalse()
    PersistentQueue<Any>().conj(1L).equiv(v(1L)).shouldBeTrue()
    PersistentQueue<Any>().conj(1L).equiv(l(1)).shouldBeTrue()
  }

  "equals" {
    (PersistentQueue<Any>().conj(1L).conj(2) == l(1)).shouldBeFalse()
    (PersistentQueue<Any>().conj(1L).conj(2) == Any()).shouldBeFalse()
    (PersistentQueue<Any>().conj(1L) == v(2)).shouldBeFalse()
    (PersistentQueue<Any>().conj(1L) == l(1)).shouldBeFalse()
    (PersistentQueue<Any>() == PersistentQueue<Any>().conj(v(1)))
      .shouldBeFalse()
    (PersistentQueue<Any>().conj(1L) == l(1L)).shouldBeTrue()
    (PersistentQueue<Any?>().conj(null) == v(null)).shouldBeTrue()
    (PersistentQueue<Any>().conj(1L).equals(Empty)).shouldBeFalse()
  }

  "toString()" {
    q().toString() shouldBe "()"
    q(l(1, 2, 3)).toString() shouldBe "(1 2 3)"
  }

  "Collection tests" - {
    "size()" {
      val queue = PersistentQueue<Int>()
        .conj(45)
        .conj(90)

      queue.size shouldBeExactly queue.count
    }

    "isEmpty()" {
      PersistentQueue<Int>().isEmpty().shouldBeTrue()
      PersistentQueue<Int>().conj(90).isEmpty().shouldBeFalse()
    }

    "contains()" {
      val queue = PersistentQueue<Int>().conj(90)

      queue.contains(90).shouldBeTrue()
      queue.contains(100).shouldBeFalse()
      PersistentQueue<Int>().contains(100).shouldBeFalse()
    }

    "containsAll()" {
      val queue = PersistentQueue<Int>().conj(1).conj(2)
      queue.containsAll(l(1, 2, 3)).shouldBeFalse()
      queue.containsAll(l(1)).shouldBeTrue()
      PersistentQueue<Int>().conj(90).containsAll(l(90)).shouldBeTrue()
      PersistentQueue<Int>().conj(90).containsAll(l(90)).shouldBeTrue()
    }

    "iterator_hasNext()" {
      PersistentQueue<Int>().conj(1).iterator().hasNext().shouldBeTrue()

      PersistentQueue<Int>().conj(1).conj(2)
        .iterator().hasNext().shouldBeTrue()

      PersistentQueue<Int>().iterator().hasNext().shouldBeFalse()
    }

    "iterator_next()" {
      val queue = PersistentQueue<Int>().conj(1).conj(2).conj(3)
      val iterator = queue.iterator()

      iterator.next() shouldBeExactly 1
      iterator.next() shouldBeExactly 2
      iterator.next() shouldBeExactly 3
      shouldThrowExactly<NoSuchElementException> { iterator.next() }

      shouldThrowExactly<NoSuchElementException> {
        PersistentQueue<Int>().iterator().next()
      }
    }
  }
})
