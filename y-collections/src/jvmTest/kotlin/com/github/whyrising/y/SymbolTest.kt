package com.github.whyrising.y

import com.github.whyrising.y.concretions.map.m
import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.util.hashCombine
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@ExperimentalStdlibApi
class SymbolTest : FreeSpec({
    "name" {
        val a = "A"

        val sym = Symbol(a)

        sym.name shouldBe a
    }

    "toString()" {
        val a = "A"
        val sym = Symbol(a)

        sym.toString() shouldBe a
    }

    "equals(other)" {
        val symbol = Symbol("A")
        (symbol == symbol).shouldBeTrue()

        (symbol.equals("A")).shouldBeFalse()

        (Symbol("A") == Symbol("A")).shouldBeTrue()
    }

    "hashCode()" {
        val a = "A"
        val hash = a.hashCode()
        val symbol = Symbol(a)
        val expected = hash xor 0 + -0x61c88647 + (hash shl 6) + (hash shr 2)

        symbol.hashCode() shouldBeExactly expected
    }

    "hasheq()" {
        val a = "A"
        val symbol = Symbol(a)
        val expected = hashCombine(Murmur3.hashUnencodedChars(a), 0)

        symbol.hasheq shouldBeExactly 0

        symbol.hasheq() shouldBeExactly expected

        symbol.hasheq shouldBeExactly expected
    }

    "compareTo(other)" {
        val a = "A"
        val symbol = Symbol(a)

        symbol.compareTo(symbol) shouldBeExactly 0

        (Symbol("A").compareTo(Symbol("A"))) shouldBeExactly 0

        (Symbol("A").compareTo(Symbol("B"))) shouldBeExactly -1

        (Symbol("B").compareTo(Symbol("A"))) shouldBeExactly 1
    }

    "invoke(map)" {
        val map = m(Symbol("A") to 1, Symbol("B") to 2)

        Symbol("A")(map)!! shouldBeExactly 1
        Symbol("B")(map)!! shouldBeExactly 2
        Symbol("Z")(map).shouldBeNull()
    }

    "invoke(map, default)" {
        val map1 = m(Symbol("A") to 1, Symbol("B") to 2, "c" to 3)
        val map2 = mapOf(Symbol("A") to 1, Symbol("B") to 2)

        Symbol("A")(map1, -1)!! shouldBeExactly 1
        Symbol("B")(map1, -1)!! shouldBeExactly 2
        Symbol("Z")(map1, null).shouldBeNull()

        Symbol("A")(map2, -1)!! shouldBeExactly 1
        Symbol("B")(map2, -1)!! shouldBeExactly 2
        Symbol("Z")(map2, null).shouldBeNull()
        Symbol("c")(map2, -1)!! shouldBeExactly -1
    }

    "s(name:String)" {
        val sym = s("A")

        sym.name shouldBe "A"
    }
})
