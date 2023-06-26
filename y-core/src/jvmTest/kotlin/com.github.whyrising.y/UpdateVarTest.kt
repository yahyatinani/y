package com.github.whyrising.y

import com.github.whyrising.y.core.ArityException
import com.github.whyrising.y.core.l
import com.github.whyrising.y.core.m
import com.github.whyrising.y.core.str
import com.github.whyrising.y.core.v
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KFunction4
import kotlin.reflect.KFunction7

class UpdateVarTest : FreeSpec({
  "updateVar(m k f)" {
    updateVar(m(":k" to 1), ":k", ::a) shouldBe m(":k" to arrayOf(1))
    updateVar(m(":k" to 1), ":k", ::b) shouldBe m(":k" to emptyArray<Any?>())

    shouldThrowExactly<ArityException> {
      updateVar(m(":k" to 1), ":k", ::c)
    }.message shouldBe "Wrong number of args 1 passed to c"
  }

  "updateVar(m k f x)" {
    updateVar(m(":k" to 1), ":k", ::a, 3) shouldBe m(":k" to arrayOf(1, 3))
    updateVar(m(":k" to 1), ":k", ::b, 3) shouldBe m(":k" to arrayOf(3))
    updateVar(m(":k" to 1), ":k", ::c, 3) shouldBe
      m(":k" to emptyArray<Any?>())

    shouldThrowExactly<ArityException> {
      updateVar(m(":k" to 1), ":k", ::d, 3)
    }.message shouldBe "Wrong number of args 2 passed to d"
  }

  "updateVar(m k f x y)" {
    updateVar(m(":k" to 1), ":k", ::a, 3, 2) shouldBe
      m(":k" to arrayOf(1, 3, 2))

    updateVar(m(":k" to 1), ":k", ::b, 3, 2) shouldBe m(":k" to arrayOf(3, 2))

    updateVar(m(":k" to 1), ":k", ::c, 3, 2) shouldBe m(":k" to arrayOf(2))

    updateVar(m(":k" to 1), ":k", ::d, 3, 2) shouldBe
      m(":k" to emptyArray<Any?>())

    shouldThrowExactly<ArityException> {
      updateVar(m(":k" to 1), ":k", ::e, 3, 2)
    }.message shouldBe "Wrong number of args 3 passed to e"
  }

  "updateVar(m k f x y z)" {
    updateVar(m(":k" to 1), ":k", ::a, 3, 2, 5) shouldBe
      m(":k" to arrayOf(1, 3, 2, 5))

    updateVar(m(":k" to 1), ":k", ::b, 3, 2, 5) shouldBe
      m(":k" to arrayOf(3, 2, 5))

    updateVar(m(":k" to 1), ":k", ::c, 3, 2, 5) shouldBe
      m(":k" to arrayOf(2, 5))

    val str4: KFunction4<Any?, Any?, Any?, Array<out Any?>, Any?> = ::str
    updateVar(m(":k" to 1), ":k", str4, 2, 3, 4) shouldBe m(":k" to "1234")

    updateVar(m(":k" to 1), ":k", str4, 2, 3, 4, 5, 6) shouldBe
      m(":k" to "123456")

    updateVar(m(":k" to 1), ":k", ::e, 3, 2, 5) shouldBe
      m(":k" to emptyArray<Any?>())

    shouldThrowExactly<ArityException> {
      updateVar(m(":k" to 1), ":k", ::f, 3, 2, 5)
    }.message shouldBe "Wrong number of args 4 passed to f"
  }

  "updateVar(m k f x y z vararg)" {
    val v7:
      KFunction7<Any?, Any?, Any?, Any?, Any?, Any?, Array<out Any?>, Any?> =
      ::v
    updateVar(m(":k" to 1), ":k", v7, 2, 3, 4, 5, 6, 7) shouldBe
      m(":k" to v(1, 2, 3, 4, 5, 6, 7))

    updateVar(m(":k" to 1), ":k", ::a, 2, 3, 4, 5, 6, 7) shouldBe
      m(":k" to v(1, 2, 3, 4, 5, 6, 7))

    updateVar(m(":k" to 1), ":k", ::b, 2, 3, 4, 5, 6, 7) shouldBe
      m(":k" to v(2, 3, 4, 5, 6, 7))

    shouldThrowExactly<NotImplementedError> {
      updateVar(m(":k" to 1), ":k", ::h, 2, 3, 4, 5, 6, 7)
    }.message shouldBe "An operation is not implemented: Arity 8 not supported"
  }

  "mapcat(f, vararg colls)" {
    mapcat(
      List<Any?>::reversed,
      v(v(3, 2, 1, 0), v(6, 5, 4), v(9, 8, 7)),
    ) shouldBe l(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

    mapcat(List<Any?>::reversed, v(v(0), v(6), v(8))) shouldBe l(0, 6, 8)

    val v2: Function2<Any?, Any?, Any?> = ::v
    mapcat(v2, v(v(3, 2), v(6, 5)), v(8, 9)) shouldBe l(v(3, 2), 8, v(6, 5), 9)

    mapcat(v2, v(v(3, 2), v(6, 5)), v(v(8, 9))) shouldBe l(v(3, 2), v(8, 9))

    mapcat(v2, v(v(3, 2), v(6, 5)), v(v(8, v(9)))) shouldBe
      l(v(3, 2), v(8, v(9)))

    val v3: Function3<Any?, Any?, Any?, Any?> = ::v
    mapcat(v3, v(v(3, 2), v(6, 5)), v(8, 9), v(12, 15)) shouldBe
      l(v(3, 2), 8, 12, v(6, 5), 9, 15)

    mapcat(v3, v(v(3, 2), v(6, 5)), v(v(8, 9)), v(12, 15)) shouldBe
      l(v(3, 2), v(8, 9), 12)

    val v4: Function4<Any?, Any?, Any?, Any?, Any?> = ::v
    mapcat(v4, v(v(3, 2), v(6, 5)), v(v(8, 9)), v(12, 15), v(12, 15)) shouldBe
      l(v(3, 2), v(8, 9), 12, 12)

    val v5: Function5<Any?, Any?, Any?, Any?, Any?, Any?> = ::v
    mapcat(
      v5,
      v(v(3, 2), v(6, 5)),
      v(v(8, 9)),
      v(12, 15),
      v(12, 15),
      v(12, 15),
    ) shouldBe l(v(3, 2), v(8, 9), 12, 12, 12)
  }

  /*  "mapcatVar()" {
      val l: Function1<Array<out Any?>, Any?> = ::l
      mapcat(
        l,
        v(":a", ":b", ":c"),
        v(1, 2, 3),
      ) shouldBe l(":a", 1, ":b", 2, ":c", 3)
    }*/
})
