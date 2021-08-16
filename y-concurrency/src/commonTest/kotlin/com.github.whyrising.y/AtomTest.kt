package com.github.whyrising.y

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class AtomTest {
    @Test
    fun `state atomic ref should be initialized while object constructing`() {
        val n = 10
        val atom = Atom(n)

        atom.state shouldBeExactly n
    }

    @Test
    fun `deref() should return the value of internal state of the atom`() {
        val n = 10
        val atom = Atom(n)

        val value = atom.deref()

        value shouldBeExactly n
        value shouldBeExactly atom.state
    }

    @Test
    fun `swap() updates the value of atom to f(current-value-of-atom)`() {
        val n = 10
        val atom = Atom(n)

        val newVal = atom.swap { currentVal ->
            currentVal + 1
        }

        newVal shouldBeExactly n + 1
    }

    @Test
    fun `validator property should be null after atom creation`() {
        val ref: IRef<Int> = Atom(10)

        ref.validator.shouldBeNull()
    }

    @Test
    fun `set validator`() {
        val ref: IRef<Int> = Atom(10)
        val vf: (Int) -> Boolean = { it > 5 }

        ref.validator = vf

        ref.validator shouldBeSameInstanceAs vf
    }

    @Test
    fun `when atom value doesn't pas validator fun, set should throw`() {
        val ref: IRef<Int> = Atom(10)
        val vf: (Int) -> Boolean = { it > 15 }

        val e = shouldThrowExactly<IllegalStateException> {
            ref.validator = vf
            vf
        }

        e.message shouldBe "Invalid reference state"
    }

    @Test
    fun `when validator fun throws, encapsulate into IllegalStateException`() {
        val ref: IRef<Int> = Atom(10)
        val vf: (Int) -> Boolean = { throw Exception("mock") }

        val e = shouldThrowExactly<IllegalStateException> {
            ref.validator = vf
            vf
        }

        e.message shouldBe "Invalid reference state"
    }

    @Test
    fun `swap(state) should throw when new value doesn't pass validation`() {
        val atom = Atom(10)
        val vf: (Int) -> Boolean = { it > 5 }
        atom.validator = vf

        val e = shouldThrowExactly<IllegalStateException> {
            atom.swap { it - 5 }
        }

        e.message shouldBe "Invalid reference state"
    }

    @Test
    fun `watches should be empty after creation of atom`() {
        val atom = Atom(10)

        val watches = atom.watches

        watches.count shouldBeExactly 0
    }

    @Test
    fun `addWatch(key, callback) should add a watch function to watches`() {
        val atom: IRef<Int> = Atom(10)
        val callback: (Any, IRef<Int>, Int, Int) -> Any = { _, _, _, _ -> }
        val key = ":key"

        val ref = atom.addWatch(key, callback)

        ref shouldBeSameInstanceAs atom
        ref.watches.count shouldBeExactly 1
        ref.watches.valAt(key) shouldBeSameInstanceAs callback
    }

    @Test
    fun `removeWatch(key, callback) should remove a watch from watches`() {
        val atom: IRef<Int> = Atom(10)
        val callback: (Any, IRef<Int>, Int, Int) -> Any = { _, _, _, _ -> }
        val key = ":key"
        atom.addWatch(key, callback)

        val ref = atom.removeWatch(key)

        ref shouldBeSameInstanceAs atom
        ref.watches.count shouldBeExactly 0
    }

    @Test
    fun `notifyWatches(oldV, newV) should call every watchFn in watches`() {
        var isWatch1Called = false
        var isWatch2Called = false
        val oldV = 10
        val newV = 20
        val k1 = ":watch1"
        val k2 = ":watch2"
        val atom: IRef<Int> = Atom(oldV)
        val watch1: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatch1Called = true

                key shouldBeSameInstanceAs k1
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        val watch2: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatch2Called = true

                key shouldBeSameInstanceAs k2
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        atom.addWatch(k1, watch1)
        atom.addWatch(k2, watch2)

        (atom as ARef<Int>).notifyWatches(oldV, newV)

        isWatch1Called.shouldBeTrue()
        isWatch2Called.shouldBeTrue()
    }

    @Test
    fun `swap(state) should notify watchers`() {
        var isWatchCalled = false
        val atom = Atom(10)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly 10
                newVal shouldBeExactly 11
            }
        atom.addWatch(k, watch)

        val newVal = atom.swap { currentVal ->
            currentVal + 1
        }

        newVal shouldBeExactly 11
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `reset(newValue) should set atom to passed value`() {
        var isWatchCalled = false
        val atom = Atom(10)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly 10
                newVal shouldBeExactly 15
            }
        atom.addWatch(k, watch)

        val newValue = atom.reset(15)

        atom.deref() shouldBeExactly 15
        newValue shouldBeExactly 15
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `compareAndSet(old,new)`() {
        var isWatchCalled = false
        val oldV = 10
        val newV = 15
        val atom = Atom(oldV)
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

        val isSet = atom.compareAndSet(oldV, newV)

        atom.deref() shouldBeExactly newV
        isSet.shouldBeTrue()
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `swap(f, arg)`() {
        var isWatchCalled = false
        val oldV = 10
        val newV = 13
        val atom = Atom(oldV)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        atom.addWatch(k, watch)

        val newVal = atom.swap(3) { currentVal, arg ->
            currentVal + arg
        }

        atom.deref() shouldBeExactly newV
        newVal shouldBeExactly newV
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `swap(f, arg1, arg2)`() {
        var isWatchCalled = false
        val oldV = 10
        val newV = 18
        val atom = Atom(oldV)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        atom.addWatch(k, watch)

        val newVal = atom.swap(3, 5) { currentVal, arg1, arg2 ->
            currentVal + arg1 + arg2
        }

        atom.deref() shouldBeExactly newV
        newVal shouldBeExactly newV
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `swapVals(f) should mutate atom and return old and new value pair`() {
        var isWatchCalled = false
        val oldV = 10
        val newV = 15
        val atom = Atom(oldV)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        atom.addWatch(k, watch)

        val pair = atom.swapVals { currentVal ->
            currentVal + 5
        }

        atom.deref() shouldBeExactly newV
        pair.first shouldBeExactly oldV
        pair.second shouldBeExactly newV
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `swapVals(arg, f)`() {
        var isWatchCalled = false
        val oldV = 10
        val newV = 13
        val atom = Atom(oldV)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        atom.addWatch(k, watch)

        val pair = atom.swapVals(3) { arg, currentVal ->
            currentVal + arg
        }

        atom.deref() shouldBeExactly newV
        pair.first shouldBeExactly oldV
        pair.second shouldBeExactly newV
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `swapVals(arg1, arg2, f)`() {
        var isWatchCalled = false
        val oldV = 10
        val newV = 17
        val atom = Atom(oldV)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly oldV
                newVal shouldBeExactly newV
            }
        atom.addWatch(k, watch)

        val pair = atom.swapVals(3, 4) { arg1, arg2, currentVal ->
            currentVal + arg1 + arg2
        }

        atom.deref() shouldBeExactly newV
        pair.first shouldBeExactly oldV
        pair.second shouldBeExactly newV
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `resetVals(newValue)`() {
        var isWatchCalled = false
        val atom = Atom(10)
        val k = ":watch"
        val watch: (Any, IRef<Int>, Int, Int) -> Any =
            { key, ref, oldVal, newVal ->
                isWatchCalled = true

                key shouldBeSameInstanceAs k
                ref shouldBeSameInstanceAs atom
                oldVal shouldBeExactly 10
                newVal shouldBeExactly 15
            }
        atom.addWatch(k, watch)

        val pair = atom.resetVals(15)

        atom.deref() shouldBeExactly 15
        pair.first shouldBeExactly 10
        pair.second shouldBeExactly 15
        isWatchCalled.shouldBeTrue()
    }

    @Test
    fun `atom()`() {
        val atom: Atom<Int> = atom(15)

        atom.swap { it * 2 }

        atom.deref() shouldBeExactly 30
    }

    @Test
    fun `invoke() should call deref()`() {
        val atm: Atom<Int> = atom(15)

        atm.swap { it * 2 }

        atm() shouldBeExactly 30
    }
}
