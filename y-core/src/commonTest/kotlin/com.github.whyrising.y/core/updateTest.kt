package com.github.whyrising.y.core

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class UpdateTest : FreeSpec({
  "update(m, k, f)" {
    update<Int>(null, ":k") { str("foo", it) } shouldBe m(":k" to "foo")

    update<Int>(m(":k" to 1), ":k") { inc(it!!) } shouldBe m(":k" to 2)
  }

  "update(m, k, x, f)" {
    update<Int, Int>(m(":k" to 1), ":k", 2) { kValue, x ->
      kValue!! + x
    } shouldBe m(":k" to 3)
  }

  "update(m, k, x, y, f)" {
    update<Int, Int, Int>(m(":k" to 1), ":k", 2, 3) { kValue, x, y ->
      kValue!! + x + y
    } shouldBe m(":k" to 6)
  }

  "update(m, k, x, y, z, f)" {
    update<Int, Int, Int, Int>(
      m = m(":k" to 1),
      k = ":k",
      x = 2,
      y = 3,
      z = 4,
    ) { kValue, x, y, z -> kValue!! + x + y + z } shouldBe m(":k" to 10)
  }

  "update(m, k, x, y, z, vararg more, f)" {
    update<Int, Int, Int, Int>(
      m = m(":k" to 1),
      k = ":k",
      x = 2,
      y = 3,
      z = 4,
      5,
      5,
    ) { kValue, x, y, z, (z1, z2) ->
      kValue!! + x + y + z + z1 as Int + z2 as Int
    } shouldBe m(":k" to 20)
  }
})
