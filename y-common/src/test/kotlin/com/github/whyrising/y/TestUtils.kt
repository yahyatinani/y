package com.github.whyrising.y

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.arb
import io.kotest.property.arbitrary.ascii
import io.kotest.property.arbitrary.merge
import io.kotest.property.arbitrary.string

fun <A> Arb.Companion.nil(): Arb<A?> = arb(listOf<A?>(null)) {
    generateSequence { null }
}

fun Arb.Companion.`string?`(
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
        Arb.`string?`().edgecases() shouldContain null
    }
})
