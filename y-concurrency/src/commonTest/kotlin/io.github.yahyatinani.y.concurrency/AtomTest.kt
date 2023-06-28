package io.github.yahyatinani.y.concurrency

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.timing.continually
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AtomTest : FreeSpec({
  "state atomic ref should be initialized while object constructing" {
    val n = 10
    val atom = Atom(n)

    atom.state shouldBe n
  }

  "deref() should return the value of internal state of the atom" {
    val n = 10
    val atom = Atom(n)

    val value = atom.deref<Int>()

    value shouldBe n
    value shouldBe atom.state
  }

  "invoke() should return the value of internal state of the atom" {
    val n = 10
    val atom = Atom(n)

    val value = atom()

    value shouldBe n
    value shouldBe atom.state
  }

  "swap() updates the value of atom to f(current-value-of-atom)" {
    val n = 10
    val atom = Atom(n)

    val newVal = atom.swap<Int> { currentVal ->
      currentVal + 1
    }

    newVal shouldBe n + 1
  }

  "validator property should be null after atom creation" {
    val ref: IRef = Atom(10)

    ref.validator.shouldBeNull()
  }

  "set validator" {
    val ref: IRef = Atom(10)
    val vf: (Any?) -> Boolean = { (it as Int) > 5 }

    ref.validator = vf

    ref.validator shouldBeSameInstanceAs vf
  }

  "when atom value doesn't pas validator fun, set should throw" {
    val ref: IRef = Atom(10)
    val vf: (Any?) -> Boolean = { (it as Int) > 15 }

    val e = shouldThrowExactly<IllegalStateException> {
      ref.validator = vf
      vf
    }

    e.message shouldBe "Invalid reference state"
  }

  "when validator fun throws, encapsulate into IllegalStateException" {
    val ref: IRef = Atom(10)
    val vf: (Any?) -> Boolean = { throw Exception("mock") }

    val e = shouldThrowExactly<IllegalStateException> {
      ref.validator = vf
      vf
    }

    e.message shouldBe "Invalid reference state"
  }

  "swap(state) should throw when new value doesn't pass validation" {
    val atom = Atom(10)
    val vf: (Any?) -> Boolean = { (it as Int) > 5 }
    atom.validator = vf

    val e = shouldThrowExactly<IllegalStateException> {
      atom.swap<Int> { it - 5 }
    }

    e.message shouldBe "Invalid reference state"
  }

  "watches should be empty after creation of atom" {
    val atom = Atom(10)

    val watches = atom.watches

    watches.count shouldBeExactly 0
  }

  "addWatch(key, callback) should add a watch function to watches" {
    val atom: IRef = Atom(10)
    val callback: (Any, IRef, Any?, Any?) -> Any = { _, _, _, _ -> }
    val key = ":key"

    val ref = atom.addWatch(key, callback)

    ref shouldBeSameInstanceAs atom
    ref.watches.count shouldBeExactly 1
    ref.watches.valAt(key) shouldBeSameInstanceAs callback
  }

  "removeWatch(key, callback) should remove a watch from watches" {
    val atom: IRef = Atom(10)
    val callback: (Any, IRef, Any?, Any?) -> Any = { _, _, _, _ -> }
    val key = ":key"
    atom.addWatch(key, callback)

    val ref = atom.removeWatch(key)

    ref shouldBeSameInstanceAs atom
    ref.watches.count shouldBeExactly 0
  }

  "notifyWatches(oldV, newV) should call every watchFn in watches" {
    var isWatch1Called = false
    var isWatch2Called = false
    val oldV = 10
    val newV = 20
    val k1 = ":watch1"
    val k2 = ":watch2"
    val atom: IRef = Atom(oldV)
    val watch1: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatch1Called = true

      key shouldBeSameInstanceAs k1
      ref shouldBeSameInstanceAs atom
      oldVal shouldBe oldV
      newVal!! shouldBe newV
    }
    val watch2: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatch2Called = true

      key shouldBeSameInstanceAs k2
      ref shouldBeSameInstanceAs atom
      oldVal shouldBe oldV
      newVal!! shouldBe newV
    }
    atom.addWatch(k1, watch1)
    atom.addWatch(k2, watch2)

    (atom as ARef).notifyWatches(oldV, newV)

    isWatch1Called.shouldBeTrue()
    isWatch2Called.shouldBeTrue()
  }

  "swap(state) should notify watchers" {
    var isWatchCalled = false
    val atom = Atom(10)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal shouldBe 10
      newVal!! shouldBe 11
    }
    atom.addWatch(k, watch)

    val newVal = atom.swap<Int> { currentVal ->
      currentVal + 1
    }

    newVal shouldBe 11
    isWatchCalled.shouldBeTrue()
  }

  "reset(newValue) should set atom to passed value" {
    var isWatchCalled = false
    val atom = Atom(10)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly 10
      newVal as Int shouldBeExactly 15
    }
    atom.addWatch(k, watch)

    val newValue = atom.reset(15)

    atom.deref() as Int shouldBeExactly 15
    newValue as Int shouldBeExactly 15
    isWatchCalled.shouldBeTrue()
  }

  "swap(f, arg)" {
    var isWatchCalled = false
    val oldV = 10
    val newV = 13
    val atom = Atom(oldV)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly oldV
      newVal as Int shouldBeExactly newV
    }
    atom.addWatch(k, watch)

    val newVal = atom.swap(3) { currentVal, arg -> (currentVal as Int) + arg }

    atom.deref() as Int shouldBeExactly newV
    newVal as Int shouldBeExactly newV
    isWatchCalled.shouldBeTrue()
  }

  "swap(f, arg1, arg2)" {
    var isWatchCalled = false
    val oldV = 10
    val newV = 18
    val atom = Atom(oldV)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly oldV
      newVal as Int shouldBeExactly newV
    }
    atom.addWatch(k, watch)

    val newVal = atom.swap(3, 5) { currentVal, arg1, arg2 ->
      (currentVal as Int) + arg1 + arg2
    }

    atom.deref() as Int shouldBeExactly newV
    newVal as Int shouldBeExactly newV
    isWatchCalled.shouldBeTrue()
  }

  "swapVals(f) should mutate atom and return old and new value pair" {
    var isWatchCalled = false
    val oldV = 10
    val newV = 15
    val atom = Atom(oldV)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly oldV
      newVal as Int shouldBeExactly newV
    }
    atom.addWatch(k, watch)

    val pair = atom.swapVals<Int> { currentVal -> currentVal + 5 }

    atom.deref() as Int shouldBeExactly newV
    pair.first shouldBeExactly oldV
    pair.second shouldBeExactly newV
    isWatchCalled.shouldBeTrue()
  }

  "swapVals(arg, f)" {
    var isWatchCalled = false
    val oldV = 10
    val newV = 13
    val atom = Atom(oldV)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly oldV
      newVal as Int shouldBeExactly newV
    }
    atom.addWatch(k, watch)

    val pair = atom.swapVals(3) { currentVal, arg ->
      (currentVal as Int) + arg
    }

    atom.deref() as Int shouldBeExactly newV
    pair.first as Int shouldBeExactly oldV
    pair.second as Int shouldBeExactly newV
    isWatchCalled.shouldBeTrue()
  }

  "swapVals(arg1, arg2, f)" {
    var isWatchCalled = false
    val oldV = 10
    val newV = 17
    val atom = Atom(oldV)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly oldV
      newVal as Int shouldBeExactly newV
    }
    atom.addWatch(k, watch)

    val pair = atom.swapVals(3, 4) { currentVal, arg1, arg2 ->
      (currentVal as Int) + arg1 + arg2
    }

    atom.deref() as Int shouldBeExactly newV
    pair.first as Int shouldBeExactly oldV
    pair.second as Int shouldBeExactly newV
    isWatchCalled.shouldBeTrue()
  }

  "resetVals(newValue)" {
    var isWatchCalled = false
    val atom = Atom(10)
    val k = ":watch"
    val watch: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
      isWatchCalled = true

      key shouldBeSameInstanceAs k
      ref shouldBeSameInstanceAs atom
      oldVal as Int shouldBeExactly 10
      newVal as Int shouldBeExactly 15
    }
    atom.addWatch(k, watch)

    val pair = atom.resetVals(15)

    atom.deref() as Int shouldBeExactly 15
    pair.first as Int shouldBeExactly 10
    pair.second as Int shouldBeExactly 15
    isWatchCalled.shouldBeTrue()
  }

  "atom()" {
    val atom: Atom = atom(15)

    atom.swap<Int> { it * 2 }

    atom.deref<Int>() shouldBeExactly 30
  }

  "invoke() should call deref()" {
    val atom: Atom = atom(15)

    atom.swap<Int> { it * 2 }

    atom.deref<Int>() shouldBeExactly 30
  }

  "Atom concurrency tests" - {
    """ should loop over and over everytime the atom value doesn't match
        the expected value due to other threads activities
    """ {
      continually(5.seconds) {
        runTest {
          val atom = Atom(0)
          val coroutinesCount = 100
          val repeatCount = 103

          val job = runParallelWork(coroutinesCount, repeatCount) {
            atom.swap<Int> { currentVal -> currentVal.inc() }
          }

          job.join()

          advanceUntilIdle()

          atom.deref<Int>() shouldBeExactly coroutinesCount * repeatCount
        }
      }
    }

    "swap(arg ,f)" {
      continually(5.seconds) {
        runTest {
          val atom = Atom(0)
          val coroutinesCount = 100
          val repeatCount = 103

          val job = runParallelWork(coroutinesCount, repeatCount) {
            atom.swap(3) { currentVal, arg -> (currentVal as Int) + arg }
          }

          job.join()
          advanceUntilIdle()

          atom.deref<Int>() shouldBeExactly coroutinesCount * repeatCount * 3
        }
      }
    }

    "swap(arg1, arg2, f)" {
      continually(5.seconds) {
        runTest {
          val atom = Atom(0)
          val coroutinesCount = 100
          val repeatCount = 103

          val job = runParallelWork(coroutinesCount, repeatCount) {
            atom.swap(3, 5) { currentVal, arg1, arg2 ->
              (currentVal as Int) + arg1 + arg2
            }
          }

          job.join()
          advanceUntilIdle()

          atom.deref<Int>() shouldBeExactly coroutinesCount * repeatCount * 8
        }
      }
    }

    "swapVals(f)" {
      continually(5.seconds) {
        runTest {
          val atom = Atom(0)
          val coroutinesCount = 100
          val repeatCount = 103

          val job = runParallelWork(coroutinesCount, repeatCount) {
            atom.swapVals<Int> { currentVal -> currentVal + 5 }
          }

          job.join()
          advanceUntilIdle()

          atom.deref<Int>() shouldBeExactly coroutinesCount * repeatCount * 5
        }
      }
    }

    "swapVals(arg, f)" {
      continually(5.seconds) {
        runTest {
          val atom = Atom(0)
          val coroutinesCount = 100
          val repeatCount = 103

          val job = runParallelWork(coroutinesCount, repeatCount) {
            atom.swapVals(3) { currentVal, arg -> (currentVal as Int) + arg }
          }

          job.join()
          advanceUntilIdle()

          atom.deref<Int>() shouldBeExactly coroutinesCount * repeatCount * 3
        }
      }
    }

    "swapVals(arg1, arg2, f)" {
      continually(5.seconds) {
        runTest {
          val atom = Atom(0)
          val coroutinesCount = 100
          val repeatCount = 103

          val job = runParallelWork(coroutinesCount, repeatCount) {
            atom.swapVals(3, 4) { currentVal, arg1, arg2 ->
              (currentVal as Int) + arg1 + arg2
            }
          }

          job.join()
          advanceUntilIdle()

          atom.deref<Int>() shouldBeExactly coroutinesCount * repeatCount * 7
        }
      }
    }
  }

  "compareAndSet" - {
    "returns true because current value is identical to oldValue" {
      var isWatchCalled = false
      val oldV: Any = 10 // Any is important here so oldV remains identical.
      val newV = 15
      val atom = atom(0)
      atom.reset(oldV)
      val k = ":watch"
      val w: (Any, IRef, Any?, Any?) -> Any = { key, ref, oldVal, newVal ->
        isWatchCalled = true

        key shouldBeSameInstanceAs k
        ref shouldBeSameInstanceAs atom
        oldVal as Int shouldBeExactly oldVal
        newVal as Int shouldBeExactly newV
      }
      atom.addWatch(k, w)

      atom.compareAndSet(oldV, newV).shouldBeTrue()
      atom.deref<Int>() shouldBeExactly newV
      isWatchCalled.shouldBeTrue()
    }
  }
})

private fun TestScope.runParallelWork(
  coroutinesCount: Int,
  repeatCount: Int,
  action: () -> Unit,
) = launch(Dispatchers.Default) {
  repeat(coroutinesCount) {
    launch {
      repeat(repeatCount) {
        action()
      }
    }
  }
}
