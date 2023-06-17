package com.github.whyrising.y.concurrency

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse

class AtomJvmTest : FreeSpec({
  "compareAndSet" - {
    "returns false because current value is not identical to oldValue" {
      val atom = atom(10)

      atom.compareAndSet(10, 15).shouldBeFalse()
    }
  }
})
