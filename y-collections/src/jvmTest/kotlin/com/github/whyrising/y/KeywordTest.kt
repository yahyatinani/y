package com.github.whyrising.y

import com.github.whyrising.y.concretions.map.m
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

@ExperimentalStdlibApi
class KeywordTest : FreeSpec({
    "ctor" {
        val key = Keyword("a")

        key.symbol shouldBe Symbol("a")
        key.hasheq shouldBeExactly Symbol("a").hasheq() + -0x61c88647
    }

    "hasheq()" {
        val key = Keyword("a")

        key.hasheq() shouldBeExactly key.hasheq
    }

    "toString()" {
        val ka = Keyword("a")

        ka.print shouldBe ""

        ka.toString() shouldBe ":a"
        ka.print shouldBe ":a"
    }

    "hashCode()" {
        val key = Keyword("a")

        key.hashCode() shouldBeExactly s("a").hashCode() + -0x61c88647
    }

    "equals(other)" {
        val key = Keyword("a")
        (key == key).shouldBeTrue()

        (key.equals("A")).shouldBeFalse()

        (Keyword("a") == Keyword("a")).shouldBeTrue()

        (Keyword("a") == Keyword("b")).shouldBeFalse()
    }

    "compareTo(other)" {
        val key = Keyword("a")

        key.compareTo(key) shouldBeExactly 0

        (Keyword("a").compareTo(Keyword("a"))) shouldBeExactly 0

        (Keyword("a").compareTo(Keyword("b"))) shouldBeExactly -1

        (Keyword("b").compareTo(Keyword("a"))) shouldBeExactly 1
    }

    "name" {
        val key = Keyword("a")

        key.name shouldBe "a"
    }

    "invoke(map)" {
        val map = m(Keyword("a") to 1, Keyword("b") to 2)

        Keyword("a")(map)!! shouldBeExactly 1
        Keyword("b")(map)!! shouldBeExactly 2
        Keyword("z")(map).shouldBeNull()
    }

    "invoke(map, default)" {
        val map1 = m(Keyword("a") to 1, Keyword("b") to 2)
        val map2 = mapOf(Keyword("a") to 1, Keyword("b") to 2)

        Keyword("a")(map1, -1)!! shouldBeExactly 1
        Keyword("b")(map1, -1)!! shouldBeExactly 2
        Keyword("z")(map1, null).shouldBeNull()

        Keyword("a")(map2, -1)!! shouldBeExactly 1
        Keyword("b")(map2, -1)!! shouldBeExactly 2
        Keyword("z")(map2, null).shouldBeNull()
        Keyword("x")(map2, -1)!! shouldBeExactly -1
    }

    "assert same key instance" {
        Keyword("a") shouldBeSameInstanceAs Keyword("a")
    }
})
