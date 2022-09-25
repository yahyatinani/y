package com.github.whyrising.y.concurrency

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class AtomTest2 {
  // TODO: 4/2/22 compareAndSet is failing on the JVM when run from FreeSpec.
  @Test
  fun compareAndSet() {
    var isWatchCalled = false
    val oldV = 10
    val newV = 15
    val atom = Atom(0)
    atom.swap { oldV }
    val k = ":watch"
    val watch: (Any, IRef<Int>, Int, Int) -> Any =
      { key, ref, oldVal, newVal ->
        isWatchCalled = true

        key shouldBeSameInstanceAs k
        ref shouldBeSameInstanceAs atom
        oldVal shouldBeExactly oldVal
        newVal shouldBeExactly newV
      }
    atom.addWatch(k, watch)

    atom.compareAndSet(oldV, newV) shouldBe true
    atom.deref() shouldBeExactly newV
    isWatchCalled.shouldBeTrue()
  }
}
