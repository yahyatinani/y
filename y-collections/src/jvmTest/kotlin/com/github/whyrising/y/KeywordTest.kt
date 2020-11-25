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
        val s = s("a")

        val key = Keyword(s)

        key.symbol shouldBeSameInstanceAs s
        key.hasheq shouldBeExactly s.hasheq() + -0x61c88647
    }

    "hasheq()" {
        val s = s("a")

        val key = Keyword(s)

        key.hasheq() shouldBeExactly key.hasheq
    }

    "toString()" {
        val ka = Keyword(s("a"))

        ka.print shouldBe ""

        ka.toString() shouldBe ":a"
        ka.print shouldBe ":a"
    }

    "hashCode()" {
        val s = s("a")
        val key = Keyword(s)

        key.hashCode() shouldBeExactly s.hashCode() + -0x61c88647
    }

    "equals(other)" {
        val key = Keyword(s("a"))
        (key == key).shouldBeTrue()

        (key.equals("A")).shouldBeFalse()

        (Keyword(s("a")) == Keyword(s("a"))).shouldBeTrue()

        (Keyword(s("a")) == Keyword(s("b"))).shouldBeFalse()
    }

    "compareTo(other)" {
        val key = Keyword(s("a"))

        key.compareTo(key) shouldBeExactly 0

        (Keyword(s("a")).compareTo(Keyword(s("a")))) shouldBeExactly 0

        (Keyword(s("a")).compareTo(Keyword(s("b")))) shouldBeExactly -1

        (Keyword(s("b")).compareTo(Keyword(s("a")))) shouldBeExactly 1
    }

    "name" {
        val key = Keyword(s("a"))

        key.name shouldBe "a"
    }

    "invoke(map)" {
        val map = m(Keyword(s("a")) to 1, Keyword(s("b")) to 2)

        Keyword(s("a"))(map)!! shouldBeExactly 1
        Keyword(s("b"))(map)!! shouldBeExactly 2
        Keyword(s("z"))(map).shouldBeNull()
    }

    "invoke(map, default)" {
        val map1 = m(Keyword(s("a")) to 1, Keyword(s("b")) to 2)
        val map2 = mapOf(Keyword(s("a")) to 1, Keyword(s("b")) to 2)

        Keyword(s("a"))(map1, -1)!! shouldBeExactly 1
        Keyword(s("b"))(map1, -1)!! shouldBeExactly 2
        Keyword(s("z"))(map1, null).shouldBeNull()

        Keyword(s("a"))(map2, -1)!! shouldBeExactly 1
        Keyword(s("b"))(map2, -1)!! shouldBeExactly 2
        Keyword(s("z"))(map2, null).shouldBeNull()
        Keyword(s("x"))(map2, -1)!! shouldBeExactly -1
    }
})
