package com.github.whyrising.y.values

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
})