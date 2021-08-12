package com.github.whyrising.y

import io.kotest.matchers.ints.shouldBeExactly
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
}
