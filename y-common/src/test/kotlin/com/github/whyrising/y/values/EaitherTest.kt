package com.github.whyrising.y.values

import com.github.whyrising.y.values.Either.Left
import com.github.whyrising.y.values.Either.Right
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class EitherTest : FreeSpec({
    "Left" - {
        "should be a subtype of Either" {
            Left::class.shouldBeSubtypeOf<Either<*, *>>()
        }

        "should be covariant" {
            val left: Left<Number, Number> = Left<Int, Double>(1)

            left.value shouldBe 1
        }

        "toString() should return `Left(value)`" {
            checkAll { i: Int ->
                Left<Int, Double>(i).toString() shouldBe "Left($i)"
            }
        }
    }

    "Right" - {
        "should be a subtype of Either" {
            Right::class.shouldBeSubtypeOf<Either<*, *>>()
        }

        "type should be covariant" {
            val right: Right<Number, Number> = Right<Int, Double>(1.0)

            right.value shouldBe 1
        }

        "toString() should return `Left(value)`" {
            checkAll { i: Double ->
                Right<Int, Double>(i).toString() shouldBe "Right($i)"
            }
        }
    }

    "left() function should return Left type as Either type" {
        Either.left<Int, Double>(1).shouldBeTypeOf<Left<Int, Double>>()
    }

    "right() function should return Right type as Either type" {
        Either.right<Int, Double>(1.0).shouldBeTypeOf<Right<Int, Double>>()
    }

    "map, Right" - {
        "when applied on Right, it should apply the transformation" {
            checkAll { i: Int ->
                val right: Either<String, Int> = Either.right(i)

                val result: Either<String, Double> = right.map { n: Int ->
                    n.toDouble()
                }

                result shouldBe Either.right(i.toDouble())
            }
        }

        "when applied on Left, it should return the original Left value" {
            checkAll { str: String ->
                val left: Either<String, Int> = Either.left(str)

                val result: Either<String, Double> = left.map { i: Int ->
                    i.toDouble()
                }

                result shouldBe left
            }
        }
    }

    "map, Left" - {
        "when applied on Left, it should apply the transformation" {
            checkAll { i: Int ->
                val left: Either<Int, String> = Either.left(i)

                val result: Either<Double, String> = left.map { n: Int ->
                    n.toDouble()
                }

                result shouldBe Either.left(i.toDouble())
            }
        }

        "when applied on Right, it should return the original Right value" {
            checkAll { str: String ->
                val right: Either<Int, String> = Either.right(str)

                val result: Either<Double, String> = right.map { i: Int ->
                    i.toDouble()
                }

                result shouldBe right
            }
        }
    }

    "flatMap, Right" - {
        "when applied on Right, it should apply the transformation" {
            checkAll { i: Int ->
                val right: Either<String, Int> = Either.right(i)

                val result: Either<String, Double> = right.flatMap { n: Int ->
                    Either.right(n.toDouble())
                }

                result shouldBe Either.right(i.toDouble())
            }
        }

        "when applied on Left, it should return the original Left value" {
            checkAll { str: String ->
                val left: Either<String, Int> = Either.left(str)

                val result: Either<String, Double> = left.flatMap { n: Int ->
                    Either.right(n.toDouble())
                }

                result shouldBe left
            }
        }
    }

    "flatMap, Left" - {
        "when applied on Left, it should apply the transformation" {
            checkAll { i: Int ->
                val left: Either<Int, String> = Either.left(i)

                val result: Either<Double, String> = left.flatMap { n: Int ->
                    Either.left(n.toDouble())
                }

                result shouldBe Either.left(i.toDouble())
            }
        }

        "when applied on Right, it should return the original Right value" {
            checkAll { str: String ->
                val right: Either<Int, String> = Either.right(str)

                val result: Either<Double, String> = right.flatMap { n: Int ->
                    Either.left(n.toDouble())
                }

                result shouldBe right
            }
        }
    }

    "getOrElse, Right" - {
        "should return the default value when called on a Left" {
            checkAll { str: String ->
                val default = -1
                val defaultValue: () -> Int = { default }
                val either: Either<String, Int> = Either.left(str)

                either.getOrElse(defaultValue) shouldBe default
            }
        }

        "should return the value when called on a Right" {
            checkAll(Arb.int().filter { it != -1 }) { i: Int ->
                val default = -1
                val defaultValue: () -> Int = { default }
                val either: Either<String, Int> = Either.right(i)

                either.getOrElse(defaultValue) shouldBe i
            }
        }
    }

    "getOrElse, Left" - {
        "should return the default value when called on a Right" {
            checkAll { str: String ->
                val default = -1
                val defaultValue: () -> Int = { default }
                val either: Either<Int, String> = Either.right(str)

                either.getOrElse(defaultValue) shouldBe default
            }
        }

        "should return the value when called on a Left" {
            checkAll(Arb.int().filter { it != -1 }) { i: Int ->
                val default = -1
                val defaultValue: () -> Int = { default }
                val either: Either<Int, String> = Either.left(i)

                either.getOrElse(defaultValue) shouldBe i
            }
        }
    }

    "orElse, Right" - {
        "should return the default value when called on a Left" {
            checkAll { value: String ->
                val default: Either<String, Int> = Either.right(-1)
                val defaultValue = { default }
                val either: Either<String, Int> = Either.left(value)

                either.orElseR(defaultValue) shouldBe default
                either.orElseR(defaultValue) shouldNotBe either
            }
        }

        "should return the value when called on a Right" {
            checkAll(Arb.int().filter { it != -1 }) { value: Int ->
                val default: Either<String, Int> = Either.right(-1)
                val defaultValue = { default }
                val either: Either<String, Int> = Either.right(value)

                either.orElseR(defaultValue) shouldBe either
                either.orElseR(defaultValue) shouldNotBe default
            }
        }
    }

    "orElse, Left" - {
        "should return the default value when called on a Right" {
            checkAll { value: String ->
                val default: Either<Int, String> = Either.left(-1)
                val defaultValue = { default }
                val either: Either<Int, String> = Either.right(value)

                either.orElseL(defaultValue) shouldBe default
                either.orElseL(defaultValue) shouldNotBe either
            }
        }

        "should return the value when called on a Left" {
            checkAll(Arb.int().filter { it != -1 }) { value: Int ->
                val default: Either<Int, String> = Either.left(-1)
                val defaultValue = { default }
                val either: Either<Int, String> = Either.left(value)

                either.orElseL(defaultValue) shouldBe either
                either.orElseL(defaultValue) shouldNotBe default
            }
        }
    }
})
