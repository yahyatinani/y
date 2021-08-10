package com.github.whyrising.y

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull

class WeakReferenceJvmTest : FreeSpec({
    "clear()" {
        val symbol = Symbol("a")
        val weakReference = WeakReference(symbol)

        weakReference.clear()

        weakReference.pointer.shouldBeNull()
        weakReference.pointer?.get().shouldBeNull()
    }
})
