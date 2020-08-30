package com.github.whyrising.y.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bool
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll

class CoreTest : FunSpec({
    context("identity") {

        test("for Int") {
            forAll(Arb.int()) { n: Int ->
                val r = identity(n)

                r == n
            }
        }

        test("for Double") {
            forAll(Arb.double()) { d: Double ->
                val r = identity(d)

                r.equals(d)
            }
        }

        test("for String") {
            forAll(Arb.string()) { s: String ->
                val r = identity(s)

                r == s
            }
        }

        test("for Boolean") {
            forAll(Arb.bool()) { b: Boolean ->
                val r = identity(b)

                r == b
            }
        }

        test("for Char") {
            forAll(Arb.char()) { c: Char ->
                val r = identity(c)

                r == c
            }
        }

        test("for List") {
            forAll(Arb.list(Arb.double())) { list: List<Double> ->
                val r = identity(list)

                r == list
            }
        }

        test("for value function") {
            val f = {}

            val r = identity(f)

            r shouldBe f
        }
    }
})
