package com.github.whyrising.y

import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class WeakReferenceNativeTest {
    @Test
    fun `clear()`() {
        val symbol = Symbol("a")
        val weakReference = WeakReference(symbol)

        weakReference.clear()

        weakReference.pointer.shouldBeNull()
        weakReference.pointer?.get().shouldBeNull()
    }
}
