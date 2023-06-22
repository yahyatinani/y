package com.github.whyrising.y.core

import com.github.whyrising.y.core.collections.IPersistentCollection
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.KFunction2
import kotlin.reflect.KFunction3
import kotlin.reflect.KFunction4

class UpdateTest : FreeSpec({
  fun inc(int: Int?): Int? = int?.inc()

  "update(m, k, f)" - {
    "asset" {
      update(m("a" to 1), "a", Int::inc) shouldBe m("a" to 2)
      update(m("a" to 1), "b", ::inc) shouldBe m("a" to 1, "b" to null)
      update(v(1, 2, 3), 1, Int::inc) shouldBe v(1, 3, 3)

      update(null, ":k") { s: String? -> str("foo", s) } shouldBe
        m(":k" to "foo")

      update(v(1, 2, 3), 2, { _: Any? -> 43 }) shouldBe v(1, 2, 43)
    }

    "when index is just past the end, you can conj to the vector" {
      update(v(1, 2, 3), 3, { _: Any? -> 43 }) shouldBe v(1, 2, 3, 43)
    }

    "when arg is not used, and index is more than one past the end" {
      shouldThrowExactly<IndexOutOfBoundsException> {
        update(v(1, 2, 3), 4, { _: Int? -> 43 })
      }
    }

    "when arg is not used, and index is one past the end of the vector" {
      shouldThrowExactly<NullPointerException> {
        update(v(1, 2, 3), 3, Int::inc)
      }
    }
  }

  "update(m, k, x, f)" {
    update(
      m(":k" to 1),
      ":k",
      { oldValue: Int, x: Int -> oldValue + x },
      2,
    ) shouldBe m(":k" to 3)

    val conj: KFunction2<IPersistentCollection<Any?>?, Any?, Any?> = ::conj
    update(m(":users" to v("user1")), ":users", conj, "user2") shouldBe
      m(":users" to v("user1", "user2"))
  }

  "update(m, k, x, y, f)" {
    update(
      m(":k" to 1),
      ":k",
      { oldValue: Int, x: Int, y: Int -> oldValue + x + y },
      2,
      3,
    ) shouldBe m(":k" to 6)

    val str: KFunction3<Any?, Any?, Any?, Any?> = ::str
    update(m(":k" to 1), ":k", str, 2, 3) shouldBe m(":k" to "123")
  }

  "update(m, k, x, y, z, f)" {
    update(
      m(":k" to 1),
      ":k",
      { oldValue: Int, x: Int, y: Int, z: Int -> oldValue + x + y + z },
      2,
      3,
      4,
    ) shouldBe m(":k" to 10)

    val vector: Function4<Any?, Any?, Any?, Any?, Any?> = ::v
    update(
      m = m(":k" to 1),
      k = ":k",
      vector,
      x = 2,
      y = 3,
      z = 4,
    ) shouldBe m(":k" to v(1, 2, 3, 4))
  }

  "update(m, k, x, y, z, vararg more, f)" {
    update(
      m(":k" to 1),
      ":k",
      { oldValue: Int, x: Int, y: Int, z: Int, z1: Int, z2: Int ->
        oldValue + x + y + z + z1 + z2
      },
      2,
      3,
      4,
      5,
      6,
    ) shouldBe m(":k" to 21)

    val vector: Function6<Any?, Any?, Any?, Any?, Any?, Any?, Any?> = ::v
    update(m(":k" to 1), ":k", vector, 2, 3, 4, 5, 6) shouldBe
      m(":k" to v(1, 2, 3, 4, 5, 6))
  }

  shouldThrowExactly<ArityException> {
    val str: KFunction4<Any?, Any?, Any?, Array<out Any?>, Any?> = ::str
    update(m(":k" to 1), ":k", str, 2, 3, 4, 5, 6)
  }.message shouldBe "Wrong number of args 6 passed to str"
})
