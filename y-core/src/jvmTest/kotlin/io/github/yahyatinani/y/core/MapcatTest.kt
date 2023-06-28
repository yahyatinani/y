package io.github.yahyatinani.y.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class MapcatTest : FreeSpec({
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

  "mapcatVar()" {
    val l: Function1<Array<out Any?>, Any?> = ::l
    shouldThrow<IllegalArgumentException> {
      mapcatVar(l)
    }.message shouldBe "mapcatVar() colls is empty"

    mapcatVar(
      l,
      v(":a", ":b", ":c"),
    ) shouldBe l(":a", ":b", ":c")

    mapcatVar(
      l,
      v(":a", ":b", ":c"),
      v(1, 2, 3),
    ) shouldBe l(":a", 1, ":b", 2, ":c", 3)

    mapcatVar(
      l,
      v(":a", ":b", ":c"),
      v(1, 2, 3),
      v(10, 11, 12),
    ) shouldBe l(":a", 1, 10, ":b", 2, 11, ":c", 3, 12)

    mapcatVar(
      l,
      v(":a", ":b", ":c"),
      v(1, 2, 3),
      v(10, 11, 12),
      v(":a", ":b", ":c"),
    ) shouldBe l(":a", 1, 10, ":a", ":b", 2, 11, ":b", ":c", 3, 12, ":c")

    mapcatVar(
      l,
      v(":a", ":b", ":c"),
      v(1, 2, 3),
      v(10, 11, 12),
      v(":a", ":b", ":c"),
      v(":a", ":b", ":c"),
    ) shouldBe
      l(
        ":a",
        1,
        10,
        ":a",
        ":a",
        ":b",
        2,
        11,
        ":b",
        ":b",
        ":c",
        3,
        12,
        ":c",
        ":c",
      )
  }
})
