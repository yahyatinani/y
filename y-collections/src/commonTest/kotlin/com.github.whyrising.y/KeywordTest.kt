package com.github.whyrising.y

import com.github.whyrising.y.concretions.map.m
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.BeforeTest
import kotlin.test.Test

class KeywordTest {
    @BeforeTest
    fun setUp() {
        (keywordsCache() as HashMap<Symbol, Any>).clear()
    }

    @ExperimentalStdlibApi
    @Test
    fun ctor() {
        val key = Keyword("a")

        key.symbol shouldBe Symbol("a")
        key.hashEq shouldBeExactly Symbol("a").hasheq() + -0x61c88647
    }

    @ExperimentalStdlibApi
    @Test
    fun hasheq() {
        val key = Keyword("a")

        key.hashEq shouldBeExactly key.hasheq()
    }

    @Test
    fun assertHashCode() {
        val key = Keyword("a")

        key.hashCode() shouldBeExactly s("a").hashCode() + -0x61c88647
    }

    @Test
    fun `equals(other)`() {
        val key = Keyword("a")

        (key == key).shouldBeTrue()
        (Keyword("a") == Keyword("a")).shouldBeTrue()

        key.equals("A").shouldBeFalse()
        (Keyword("a") == Keyword("b")).shouldBeFalse()
    }

    @Test
    fun `compareTo(other)`() {
        Keyword("a").compareTo(Keyword("a")) shouldBeExactly 0
        Keyword("a").compareTo(Keyword("b")) shouldBeLessThan 0
        Keyword("b").compareTo(Keyword("a")) shouldBeGreaterThan 0
    }

    @Test
    fun `name property`() {
        val key = Keyword("a")

        key.name shouldBe "a"
    }

    @Test
    fun `invoke(map)`() {
        val map = m(Keyword("a") to 1, Keyword("b") to 2, "c" to 3)

        Keyword("a")(map)!! shouldBe 1
        Keyword("b")(map)!! shouldBe 2
        Keyword("c")(map).shouldBeNull()
        Keyword("z")(map).shouldBeNull()
    }

    @Test
    fun `invoke(map, default)`() {
        val map1 = m(Keyword("a") to 1, Keyword("b") to 2)
        val map2 = mapOf(Keyword("a") to 1, Keyword("b") to 2)

        Keyword("a")(map1, -1)!! shouldBe 1
        Keyword("b")(map1, -1)!! shouldBe 2
        Keyword("z")(map1, null).shouldBeNull()

        Keyword("a")(map2, -1)!! shouldBe 1
        Keyword("b")(map2, -1)!! shouldBe 2
        Keyword("z")(map2, null).shouldBeNull()
        Keyword("x")(map2, -1)!! shouldBe -1
    }

    @Test
    fun `assert same key instance`() {
        Keyword("a") shouldBeSameInstanceAs Keyword("a")
    }

    @Test
    fun `k(a)`() {
        val key = k("a")

        Keyword("a") shouldBe key
    }

    @Test
    fun `toString() should return print property`() {
        val a = Keyword("a")
        val b = Keyword("b")

        a.toString() shouldBe ":a"
        a.print shouldBe ":a"
        b.toString() shouldBe ":b"
        b.print shouldBe ":b"
    }
}