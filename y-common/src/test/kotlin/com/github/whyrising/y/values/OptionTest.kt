package com.github.whyrising.y.values

import com.github.whyrising.y.core.str
import com.github.whyrising.y.values.Option.None
import com.github.whyrising.y.values.Option.Some
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class OptionTest : FreeSpec({
    "None type" - {
        "should be a singleton" {
            val non1 = Option<Int>()
            val non2 = Option<String>()

            non1 shouldBe non2
            non1 shouldBeSameInstanceAs non2
        }

        "hashCode should be 0" {
            None.hashCode() shouldBeExactly 0
        }

        "toString should return `None`" {
            None.toString() shouldBe "None"
        }

        "should be a subtype of Option" {
            None::class.shouldBeSubtypeOf<Option<*>>()
        }
    }

    "`Some` type should be a subtype of Option" {
        Some::class.shouldBeSubtypeOf<Option<*>>()
    }

    "`Some type should be a data class`" {
        Some(1) shouldBe Some(1)
        Some(1.0) shouldBe Some(1.0)
        Some("s") shouldBe Some("s")
    }

    "`Some` type should be covariant" {
        val some: Some<Number> = Some<Int>(1)
        some.value shouldBe 1
    }

    "Option type companion constructor" - {
        "invoke with no arguments should return None type" {
            val option: Option<Nothing> = Option.invoke()

            option.shouldBeTypeOf<None>()
        }

        "invoke with non-null argument should return `Some` type" {
            checkAll { a: Int, b: String ->
                val option1: Option<Int> = Option.invoke(a)
                val option2: Option<String> = Option.invoke(b)

                option1.shouldBeTypeOf<Some<Int>>()
                option1.value shouldBeExactly a

                option2.shouldBeTypeOf<Some<String>>()
                option2.value shouldBe b
            }
        }

        "invoke with a null argument should return `Some` type" {
            val option: Option<Int> = Option.invoke(null)

            option.shouldBeTypeOf<None>()
        }
    }

    "isEmpty()" - {
        "should return true" {
            val option: Option<Int> = Option()

            option.isEmpty().shouldBeTrue()
        }

        "should return false" {
            val option = Option(1)

            option.isEmpty().shouldBeFalse()
        }
    }

    "getOrElse" - {
        "should return the value" {
            checkAll(Arb.int(), Arb.string()) { i: Int, str: String ->
                val option1: Option<Int> = Option(i)
                val option2: Option<String> = Option(str)

                option1.getOrElse { 0 } shouldBe i
                option2.getOrElse { "" } shouldBe str
            }
        }

        "should return the default value when passed a null" {
            val default1 = 0
            val default2 = ""
            val option1: Option<Int> = Option(null)
            val option2: Option<String> = Option(null)

            option1.getOrElse { default1 } shouldBe default1
            option2.getOrElse { default2 } shouldBe default2
        }
    }

    "map" - {
        "when `this` is None, should return None" {
            val option: Option<Int> = Option()

            val mapped: Option<String> = option.map { i: Int -> "$i" }

            mapped shouldBeSameInstanceAs None
        }

        "when `this` is Some, should return the mapped value" {
            checkAll { i: Int ->
                val f: (Int) -> String = { n: Int -> "$n" }
                val option: Option<Int> = Option(i)

                val mapped: Option<String> = option.map(f)

                mapped shouldBe Option(f(i))
            }
        }
    }

    "flatMap" - {
        val f: (Int) -> Option<String> = { i: Int -> Option("$i") }

        "when `this` is None, should return None" {
            val option: Option<Int> = Option()

            val mapped: Option<String> = option.flatMap(f)

            mapped shouldBeSameInstanceAs None
        }

        "when `this` is Some, should return the mapped value" {
            checkAll { i: Int ->
                val option: Option<Int> = Option(i)

                val mapped: Option<String> = option.flatMap(f)

                mapped shouldBe f(i)
            }
        }
    }

    "orElse" - {
        "should return the default Option" {
            val option: Option<Int> = Option()
            val default: () -> Option<Int> = { Option(0) }

            val r: Option<Int> = option.orElse(default)

            r shouldBe default()
        }

        "should return the value Option" {
            val option: Option<Int> = Option(2)
            val default: () -> Option<Int> = { Option(0) }

            val r: Option<Int> = option.orElse(default)

            r shouldBe option
        }
    }

    "filter" {
        checkAll { i: Int ->
            val p: (Int) -> Boolean = { it % 2 == 0 }
            val option: Option<Int> = Option(i)

            val even = option.filter(p)

            when (p(i)) {
                true -> even shouldBe option
                else -> even shouldBe None
            }
        }
    }

    "lift" - {
        "should convert f: A -> B, to g: Option(A) -> Option(B)" {
            checkAll { n: Int, l: Long ->
                val f1: (Int) -> String = { i: Int -> str(i) }
                val f2: (Long) -> String = { i: Long -> str(i) }

                val g1: (Option<Int>) -> Option<String> = lift(f1)
                val g2: (Option<Long>) -> Option<String> = lift(f2)

                g1(Option(n)) shouldBe Option(f1(n))
                g2(Option(l)) shouldBe Option(f2(l))
            }
        }

        "should convert f: A,B -> C, to g: Option(A),Option(B) -> Option(C)" {
            checkAll { n: Int, l: Long, f: Float ->
                val f1: (Int) -> (Float) -> String = { { f -> str(it, f) } }
                val f2: (Long) -> (Float) -> String = { { f -> str(it, f) } }

                val g1: (Option<Int>) ->
                (Option<Float>) ->
                Option<String> = lift(f1)

                val g2: (Option<Long>) ->
                (Option<Float>) ->
                Option<String> = lift(f2)

                g1(Option(n))(Option(f)) shouldBe Option(f1(n)(f))
                g2(Option(l))(Option(f)) shouldBe Option(f2(l)(f))
            }
        }

        "should convert f: A,B,C -> D, " +
            "to g: Option(A),Option(B),Option(C) -> Option(D)" {
                checkAll { n: Int, x: Float, y: Double ->
                    val f: (Int) ->
                    (Float) ->
                    (Double) ->
                    String = { { f -> { d -> str(it, f, d) } } }

                    val g: (Option<Int>) ->
                    (Option<Float>) ->
                    (Option<Double>) ->
                    Option<String> = lift(f)

                    g(Option(n))(Option(x))(Option(y)) shouldBe
                        Option(f(n)(x)(y))
                }
            }
    }

    "hLift" - {
        "should convert f: A -> B to g: A -> Option(B)" - {
            checkAll { n: Int, l: Long ->
                val f1: (Int) -> String = { i: Int -> str(i) }
                val f2: (Long) -> String = { i: Long -> str(i) }

                val g1: (Int) -> Option<String> = hLift(f1)
                val g2: (Long) -> Option<String> = hLift(f2)

                g1(n) shouldBe Option(f1(n))
                g2(l) shouldBe Option(f2(l))
            }
        }
    }
})
