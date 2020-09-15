package com.github.whyrising.y.values

fun <T> assertCondition(value: T, msg: String, p: (T) -> Boolean): Result<T> =
    Result.of(value, msg, p)

private fun defaultBoolErr(b: Boolean) = "condition should be $b"

fun <T> assertCondition(value: T, p: (T) -> Boolean): Result<T> {
    val b = true
    return Result.of(value, defaultBoolErr(b), p)
}

fun assertTrue(
    condition: Boolean,
    errMsg: String = defaultBoolErr(true)
): Result<Boolean> = assertCondition(condition, errMsg) { it }

fun assertFalse(
    condition: Boolean,
    errMsg: String = defaultBoolErr(false)
): Result<Boolean> = assertCondition(condition, errMsg) { !it }

fun <T> assertNotNull(value: T, errMsg: String): Result<T> =
    assertCondition(value, errMsg) { it != null }

fun <T> assertNotNull(value: T): Result<T> =
    assertNotNull(value, "object should not be null")

fun assertPositive(
    n: Int,
    errMsg: String = "$n must be positive"
): Result<Int> = assertCondition(n, errMsg) { it > 0 }

fun assertInRange(n: Int, min: Int, max: Int): Result<Int> =
    assertCondition(n, "$n should be > $min and < $max") {
        it in (min + 1) until max
    }

fun assertPositiveOrZero(
    n: Int,
    errMsg: String = "$n should be >= 0"
): Result<Int> = assertCondition(n, errMsg) { it >= 0 }
