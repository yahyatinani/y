package com.github.whyrising.y

import com.github.whyrising.y.core.l
import com.github.whyrising.y.core.util.Murmur3
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly

class Murmur3Test : FreeSpec({
  "Murmur3.hashOrdered(x)" {
    val l1 = l("Mango", 1, 32569885145, 12.toShort(), -0.0f, true)

    Murmur3.hashOrdered(l1) shouldBeExactly 740609528
  }
})
