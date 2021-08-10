package com.github.whyrising.y

import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class WeakReferenceTest {
    @Test
    fun `value should return the same instance that was given to the ref`() {
        val symbol = Symbol("a")

        val weakReference = WeakReference(symbol)

        weakReference.value shouldBeSameInstanceAs symbol
    }
}
