package com.github.whyrising.y

import com.github.whyrising.y.collections.Keyword
import com.github.whyrising.y.collections.k
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KeywordTest : FreeSpec({
    beforeTest {
        Keyword.cache.clear()
    }

    "ctor" {
        val key = Keyword("a")

        key.symbol shouldBe Symbol("a")
        key.hashEq shouldBeExactly Symbol("a").hasheq() + -0x61c88647
    }

    "hasheq" {
        val key = Keyword("a")

        key.hashEq shouldBeExactly key.hasheq()
    }

    "assertHashCode" {
        val key = Keyword("a")

        key.hashCode() shouldBeExactly s("a").hashCode() + -0x61c88647
    }

    "equals(other)" {
        val key = Keyword("a")

        (key == key).shouldBeTrue()
        (Keyword("a") == Keyword("a")).shouldBeTrue()

        key.equals("A").shouldBeFalse()
        (Keyword("a") == Keyword("b")).shouldBeFalse()
    }

    "compareTo(other)" {
        Keyword("a").compareTo(Keyword("a")) shouldBeExactly 0
        Keyword("a").compareTo(Keyword("b")) shouldBeLessThan 0
        Keyword("b").compareTo(Keyword("a")) shouldBeGreaterThan 0
    }

    "name property" {
        val key = Keyword("a")

        key.name shouldBe "a"
    }

    "invoke(map)" {
        val map = m(Keyword("a") to 1, Keyword("b") to 2, "c" to 3)
        val set = hashSet(Keyword("a"), Keyword("b"), Keyword("c"))

        Keyword("a")<Int>(map)!! shouldBe 1
        Keyword("b")<Int>(map)!! shouldBe 2
        Keyword("c")<Int>(map).shouldBeNull()
        Keyword("z")<Int>(map).shouldBeNull()

        Keyword("a")<Int>(set) shouldBe Keyword("a")
        Keyword("b")<Int>(set) shouldBe Keyword("b")
    }

    "invoke(map, default)" {
        val map1 = m(Keyword("a") to 1, Keyword("b") to 2)
        val map2 = mapOf(Keyword("a") to 1, Keyword("b") to 2)
        val set = hashSet(Keyword("a"), Keyword("b"), Keyword("c"))

        Keyword("a")(map1, -1)!! shouldBe 1
        Keyword("b")(map1, -1)!! shouldBe 2
        Keyword("z")<Int?>(map1, null).shouldBeNull()

        Keyword("a")(map2, -1)!! shouldBe 1
        Keyword("b")(map2, -1)!! shouldBe 2
        Keyword("z")<Int?>(map2, null).shouldBeNull()
        Keyword("x")(map2, -1)!! shouldBe -1

        Keyword("x")(set, -1)!! shouldBe -1
    }

    "assert same key instance" {
        Keyword("a") shouldBeSameInstanceAs Keyword("a")
    }

    "k(a)" {
        val key = k("a")

        Keyword("a") shouldBe key
    }

    "toString() should return print property" {
        val a = Keyword("a")
        val b = Keyword("b")

        a.toString() shouldBe ":a"
        a.str shouldBe ":a"
        b.toString() shouldBe ":b"
        b.str shouldBe ":b"
    }

    "serialize" {
        val keyword = k("a")

        Json.encodeToString(keyword) shouldBe "\"a\""
    }

    "deserialize" {
        val stringKey = "\"a\""

        val decodedKey = Json.decodeFromString<Keyword>(stringKey)

        decodedKey shouldBeSameInstanceAs k("a")
    }
})
