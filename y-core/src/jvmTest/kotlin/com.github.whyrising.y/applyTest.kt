package com.github.whyrising.y

import com.github.whyrising.y.core.ArityException
import com.github.whyrising.y.core.v
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

fun a(vararg args: Any?): Any = args
fun b(x: Any?, vararg args: Any?): Any = args
fun c(x: Any?, y: Any?, vararg args: Any?): Any = args
fun d(x: Any?, y: Any?, z: Any?, vararg args: Any?): Any = args
fun e(q: Any?, b: Any?, c: Any?, d: Any?, vararg args: Any?): Any = args
fun f(q: Any?, b: Any?, c: Any?, d: Any?, e: Any?, vararg args: Any?): Any =
  args

fun g(
  q: Any?,
  b: Any?,
  c: Any?,
  d: Any?,
  e: Any?,
  f: Any?,
  vararg args: Any?,
): Any = args

fun h(
  q: Any?,
  b: Any?,
  c: Any?,
  d: Any?,
  e: Any?,
  f: Any?,
  g: Any?,
  vararg args: Any?,
): Any = args

class ApplyTest : FreeSpec({
  "applyVar(f, args)" - {
    "f(vararg args)" {
      applyVar(::a, null) shouldBe emptyArray<Any?>()
      applyVar(::a, v<Any?>()) shouldBe emptyArray<Any?>()
      applyVar(::a, v(1, 2)) shouldBe arrayOf(1, 2)
    }

    "f(x, vararg args)" {
      applyVar(::b, v(1, 2)) shouldBe arrayOf(2)
      applyVar(::b, v(1, 2, 3, 4)) shouldBe arrayOf(2, 3, 4)
      applyVar(::b, v(1)) shouldBe emptyArray<Any?>()
      applyVar(::b, v(null)) shouldBe emptyArray<Any?>()
      applyVar(::b, v(null, null)) shouldBe arrayOf<Any?>(null)

      shouldThrowExactly<ArityException> { applyVar(::b, null) }
        .message shouldBe "Wrong number of args 0 passed to ${::b.name}"

      shouldThrowExactly<ArityException> { applyVar(::b, v<Any?>()) }
        .message shouldBe "Wrong number of args 0 passed to ${::b.name}"
    }

    "f(x, y, vararg args)" {
      applyVar(::c, v(1, 2, 3, 4)) shouldBe arrayOf(3, 4)
      applyVar(::c, v(1, 2)) shouldBe emptyArray<Any?>()
      applyVar(::c, v(null, null)) shouldBe emptyArray<Any?>()
      applyVar(::c, v(null, null, null)) shouldBe arrayOf<Any?>(null)

      shouldThrowExactly<ArityException> { applyVar(::c, null) }
        .message shouldBe "Wrong number of args 0 passed to ${::c.name}"

      shouldThrowExactly<ArityException> { applyVar(::c, v<Any?>()) }
        .message shouldBe "Wrong number of args 0 passed to ${::c.name}"

      shouldThrowExactly<ArityException> { applyVar(::c, v(1)) }
        .message shouldBe "Wrong number of args 1 passed to ${::c.name}"
    }

    "f(x, y, z, vararg args)" {
      val f = ::d
      applyVar(f, v(1, 2, 3, 4, 6)) shouldBe arrayOf(4, 6)
      applyVar(f, v(1, 2, 3)) shouldBe emptyArray<Any?>()
      applyVar(f, v(null, null, null)) shouldBe emptyArray<Any?>()
      applyVar(f, v(null, null, null, null)) shouldBe arrayOf<Any?>(null)

      shouldThrowExactly<ArityException> { applyVar(f, null) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v<Any?>()) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v(1, 2)) }
        .message shouldBe "Wrong number of args 2 passed to ${f.name}"
    }

    "f(a, b, c, d, vararg args)" {
      val f = ::e
      applyVar(f, v(1, 2, 3, 4, 6, 8)) shouldBe arrayOf(6, 8)
      applyVar(f, v(1, 2, 3, 4, null)) shouldBe arrayOf<Any?>(null)
      applyVar(f, v(1, 2, 3, 4)) shouldBe emptyArray<Any?>()
      applyVar(f, v(null, null, null, null)) shouldBe emptyArray<Any?>()

      shouldThrowExactly<ArityException> { applyVar(f, null) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v<Any?>()) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v(1, 2, 3)) }
        .message shouldBe "Wrong number of args 3 passed to ${f.name}"
    }

    "f(a, b, c, d, e, vararg args)" {
      val f = ::f
      applyVar(f, v(1, 2, 3, 4, 6, 8, 9)) shouldBe arrayOf(8, 9)
      applyVar(f, v(1, 2, 3, 4, 5, null)) shouldBe arrayOf<Any?>(null)
      applyVar(f, v(1, 2, 3, 4, 5)) shouldBe emptyArray<Any?>()
      applyVar(f, v(null, null, null, null, null)) shouldBe emptyArray<Any?>()

      shouldThrowExactly<ArityException> { applyVar(f, null) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v<Any?>()) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v(1, 2, 3, 4)) }
        .message shouldBe "Wrong number of args 4 passed to ${f.name}"
    }

    "f(a, b, c, d, e, f, vararg args)" {
      val f = ::g
      applyVar(f, v(1, 2, 3, 4, 6, 8, 9, 11)) shouldBe arrayOf(9, 11)
      applyVar(f, v(1, 2, 3, 4, 5, 7, null)) shouldBe arrayOf<Any?>(null)
      applyVar(f, v(1, 2, 3, 4, 5, 2)) shouldBe emptyArray<Any?>()
      applyVar(f, v(null, null, null, null, null, null)) shouldBe
        emptyArray<Any?>()

      shouldThrowExactly<ArityException> { applyVar(f, null) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v<Any?>()) }
        .message shouldBe "Wrong number of args 0 passed to ${f.name}"

      shouldThrowExactly<ArityException> { applyVar(f, v(1, 2, 3, 4, 5)) }
        .message shouldBe "Wrong number of args 5 passed to ${f.name}"
    }

    "more than 7" {
      shouldThrowExactly<NotImplementedError> {
        applyVar(::h, v(1, 2, 3, 4, 6, 8, 9, 11))
      }.message shouldBe
        "An operation is not implemented: Arity 8 not supported"
    }
  }

  "applyVar(f, x , args)" - {
    "f(vararg args)" {
      applyVar(::a, 1, null) shouldBe arrayOf<Any?>(1)
      applyVar(::a, 1, v(null)) shouldBe arrayOf<Any?>(1, null)
      applyVar(::a, null, v(null)) shouldBe arrayOf<Any?>(null, null)
      applyVar(::a, 1, v(2, 3, 4)) shouldBe arrayOf<Any?>(1, 2, 3, 4)
      applyVar(::a, 1, v<Any?>()) shouldBe arrayOf<Any?>(1)
      applyVar(::a, null, v<Any?>()) shouldBe arrayOf<Any?>(null)
    }

    "f(x, vararg args)" {
      applyVar(::b, 1, v(1, 2)) shouldBe arrayOf(1, 2)
      applyVar(::b, 1, v<Any?>()) shouldBe emptyArray<Any?>()
      applyVar(::b, 1, v<Any?>(null)) shouldBe arrayOf<Any?>(null)
      applyVar(::b, null, v<Any?>(null)) shouldBe arrayOf<Any?>(null)
    }
  }

  "applyVar(f, x , y, args)" - {
    "f(vararg args)" {
      applyVar(::a, 1, 2, null) shouldBe arrayOf<Any?>(1, 2)
      applyVar(::a, 1, 2, v(null)) shouldBe arrayOf<Any?>(1, 2, null)
      applyVar(::a, null, null, v(null)) shouldBe
        arrayOf<Any?>(null, null, null)
      applyVar(::a, 1, 2, v(2, 3, 4)) shouldBe arrayOf<Any?>(1, 2, 2, 3, 4)
      applyVar(::a, 1, 2, v<Any?>()) shouldBe arrayOf<Any?>(1, 2)
      applyVar(::a, null, null, v<Any?>()) shouldBe arrayOf<Any?>(null, null)
    }

    "f(x, vararg args)" {
      applyVar(::b, null, null, v<Any?>()) shouldBe arrayOf<Any?>(null)
      applyVar(::b, 1, 3, v(1, 2)) shouldBe arrayOf(3, 1, 2)
      applyVar(::b, 1, 3, v<Any?>()) shouldBe arrayOf(3)
      applyVar(::b, 1, 2, v<Any?>(null)) shouldBe arrayOf(2, null)
      applyVar(::b, null, null, v<Any?>(null)) shouldBe
        arrayOf<Any?>(null, null)
    }
  }

  "applyVar(f, x , y, z, args)" - {
    "f(vararg args)" {
      applyVar(::a, 1, 2, 3, null) shouldBe arrayOf<Any?>(1, 2, 3)
      applyVar(::a, 1, 2, 3, v(null)) shouldBe arrayOf<Any?>(1, 2, 3, null)
      applyVar(::a, null, null, null, v(null)) shouldBe
        arrayOf<Any?>(null, null, null, null)
      applyVar(::a, 1, 2, 3, v(2, 3, 4)) shouldBe
        arrayOf<Any?>(1, 2, 3, 2, 3, 4)
      applyVar(::a, 1, 2, 3, v<Any?>()) shouldBe arrayOf<Any?>(1, 2, 3)
      applyVar(::a, null, null, null, v<Any?>()) shouldBe
        arrayOf<Any?>(null, null, null)
    }
  }

  "applyVar(f, a, b, c, d, args)" - {
    "f(vararg args)" {
      applyVar(::a, 1, 2, 3, 4, null) shouldBe arrayOf<Any?>(1, 2, 3, 4)
      applyVar(::a, 1, 2, 3, 4, v(null)) shouldBe
        arrayOf<Any?>(1, 2, 3, 4, null)
      applyVar(::a, null, null, null, null, v(null)) shouldBe
        arrayOf<Any?>(null, null, null, null, null)
      applyVar(::a, 1, 2, 3, 4, v(2, 3, 4)) shouldBe
        arrayOf<Any?>(1, 2, 3, 4, 2, 3, 4)
      applyVar(::a, 1, 2, 3, 4, v<Any?>()) shouldBe arrayOf<Any?>(1, 2, 3, 4)
      applyVar(::a, null, null, null, null, v<Any?>()) shouldBe
        arrayOf<Any?>(null, null, null, null)
    }
  }
})
