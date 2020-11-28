package com.github.whyrising.y

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.property.Arb
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.ascii
import io.kotest.property.arbitrary.merge
import io.kotest.property.arbitrary.string

val isEven: (Int) -> Boolean = { it % 2 == 0 }
val idOdd = complement(isEven)

fun <A> Arb.Companion.nil(): Arb<A?> = arb(
    object : Shrinker<A?> {
        override fun shrink(value: A?): List<A?> = listOf(null)
    },
    listOf(null)
) { null }

fun Arb.Companion.stringUnull(
    minSize: Int = 0,
    maxSize: Int = 100,
    codepoints: Arb<Codepoint> = Arb.ascii()
): Arb<String?> = Arb.nil<String>()
    .merge(Arb.string(minSize, maxSize, codepoints))

class TestUtilsTest : FreeSpec({
    "Arb nil generator should return null" {
        Arb.nil<Int>().edgecases() shouldContain null
        Arb.nil<String>().edgecases() shouldContain null
        Arb.nil<Boolean>().edgecases() shouldContain null
    }

    "Arb string? generator should generate at least 1 null" {
        Arb.stringUnull().edgecases() shouldContain null
    }
})

val foo1 = { _: Int, _: Double -> 1 }

val foo2 = { _: Int, _: Double, _: Float -> 1 }

val foo3 = { _: Int, _: Double, _: Float, _: String -> 1 }

val foo4 = { _: Int, _: Double, _: Float, _: String, _: Boolean -> 1 }

val foo5 = { _: Int, _: Double, _: Float, _: String, _: Boolean, _: Long -> 1 }
