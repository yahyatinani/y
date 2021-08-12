package com.github.whyrising.y

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.atomicfu.AtomicRef
import kotlin.test.Test

class AtomTest {
    @Test
    fun `state atomic ref should be initialized while object constructing`() {
        val n = 10
        val atom = Atom(n)
        val state: AtomicRef<Int> = atom.state

        state.value shouldBeExactly n
    }

    @Test
    fun `deref() should return the value of internal state of the atom`() {
        val n = 10
        val atom = Atom(n)

        val value = atom.deref()

        value shouldBeExactly n
        value shouldBeExactly atom.state.value
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
        val n = 10
        val ref: IRef<Int> = Atom(n)

        ref.validator.shouldBeNull()
    }

    @Test
    fun `set validator`() {
        val n = 10
        val ref: IRef<Int> = Atom(n)
        val vf: (Int) -> Boolean = { it > 5 }

        ref.validator = vf

        ref.validator shouldBeSameInstanceAs vf
    }

    @Test
    fun `when atom value doesn't pas validator fun, set should throw`() {
        val n = 10
        val ref: IRef<Int> = Atom(n)
        val vf: (Int) -> Boolean = { it > 15 }

        val e = shouldThrowExactly<IllegalStateException> {
            ref.validator = vf
            vf
        }

        e.message shouldBe "Invalid reference state"
    }

    @Test
    fun `when validator fun throws, encapsulate into IllegalStateException`() {
        val n = 10
        val ref: IRef<Int> = Atom(n)
        val vf: (Int) -> Boolean = { throw Exception("mock") }

        val e = shouldThrowExactly<IllegalStateException> {
            ref.validator = vf
            vf
        }

        e.message shouldBe "Invalid reference state"
    }

    @Test
    fun `swap(state) should throw when new value doesn't pass validation`() {
        val n = 10
        val atom = Atom(n)
        val vf: (Int) -> Boolean = { it > 5 }
        atom.validator = vf

        val e = shouldThrowExactly<IllegalStateException> {
            atom.swap { it - 5 }
        }

        e.message shouldBe "Invalid reference state"
    }
}
