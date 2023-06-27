package com.github.whyrising.y

import com.github.whyrising.y.core.l
import com.github.whyrising.y.core.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction4

fun addArgs(x: Int, y: Int, z: Int, vararg more: Any): Int =
  more.fold(x + y + z) { acc, i -> acc + i as Int }

class UpdateInVar : FreeSpec({
  val f1: KFunction1<Array<out Any?>, Any> = ::l
  "updateInVar()" {
    updateInVar(m(":a" to m(":b" to 2), ":c" to 5), l(":a", ":b"), f1) shouldBe
      m(":a" to m(":b" to l(2)), ":c" to 5)

    val f: KFunction4<Int, Int, Int, Array<out Any>, Int> = ::addArgs
    updateInVar(m(":a" to 3), l(":a"), f, 7, 1, 2, 3) shouldBe
      m(":a" to 16)

    updateInVar(m(":a" to 3), l(":a"), f1, 1, 2, 4) shouldBe
      m(":a" to l(3, 1, 2, 4))
  }
})
