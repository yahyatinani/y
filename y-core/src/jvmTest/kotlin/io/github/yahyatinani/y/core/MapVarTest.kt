package io.github.yahyatinani.y.core

import io.github.yahyatinani.y.core.collections.PersistentList
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction4

fun stringify2(x: Any?, vararg y: Any?): String =
  y.fold(x.toString()) { acc, arg -> "$acc$arg" }

fun stringify3(x: Any?, y: Any?, vararg more: Any?): String =
  more.fold("$x$y") { acc, arg -> "$acc$arg" }

class MapVarTest : FreeSpec({
  "mapVar()" - {
    "mapVar(f, coll)" {
      val l: KFunction1<Array<out Any?>, Any?> = ::l
      val mapVar = mapVar(l, v(1, 2, 3, 4))
      (mapVar.first() as PersistentList<Any?>).first() shouldBe 1
      mapVar shouldBe l(l(1), l(2), l(3), l(4))

      mapVar(l, listOf(1, 2, 3, 4)) shouldBe l(l(1), l(2), l(3), l(4))
    }

    "mapVar(f, coll1,coll2)" {
      mapVar(::stringify2, v(1, 2), v(3, 4)) shouldBe l("13", "24")

      val l: KFunction1<Array<out Any?>, Any?> = ::l
      mapVar(l, v(1, 2), v(3, 4)) shouldBe l(l(1, 3), l(2, 4))
      mapVar(l, listOf(1, 2), listOf(3, 4)) shouldBe l(l(1, 3), l(2, 4))
    }

    "mapVar(f, coll1, coll2, coll3)" {
      mapVar(::stringify3, v(1, 2), v(3, 4), v(5, 6)) shouldBe l("135", "246")

      val l: KFunction1<Array<out Any?>, Any?> = ::l
      mapVar(l, v(1, 2), v(3, 4), v(5, 6)) shouldBe l(l(1, 3, 5), l(2, 4, 6))
    }

    "mapVar(f, coll1, coll2, coll3, vararg colls)" {
      val str: KFunction4<Any?, Any?, Any?, Array<out Any?>, String> = ::str
      mapVar(str, v(1, 2), v(3, 4), v(5, 6), v(5, 6)) shouldBe l("1355", "2466")
      val l: KFunction1<Array<out Any?>, Any?> = ::l

      mapVar(l, v(1, 2), v(3, 4), v(5, 6), v(5, 6)) shouldBe
        l(l(1, 3, 5, 5), l(2, 4, 6, 6))
    }
  }
})
