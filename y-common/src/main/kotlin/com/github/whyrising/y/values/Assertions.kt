package com.github.whyrising.y.values

fun <T> assertCondition(value: T, msg: String, p: (T) -> Boolean): Result<T> =
    Result.of(value, msg, p)

fun <T> assertCondition(value: T, p: (T) -> Boolean): Result<T> =
    Result.of(value, "condition should be true", p)

fun assertTrue(
    condition: Boolean,
    errMsg: String = "condition should be true"
): Result<Boolean> = assertCondition(condition, errMsg) { it }

fun assertFalse(
    condition: Boolean,
    errMsg: String = "condition should be false"
): Result<Boolean> = assertCondition(condition, errMsg) { !it }

fun <T> assertNotNull(value: T, errMsg: String): Result<T> =
    assertCondition(value, errMsg) { it != null }

fun <T> assertNotNull(value: T): Result<T> =
    assertNotNull(value, "object should not be null")

fun assertPositive(n: Int, errMsg: String = "number should be n"): Result<Int> =
    assertCondition(n, errMsg) { it > 0 }

fun assertInRange(n: Int, min: Int, max: Int): Result<Int> =
    assertCondition(n, "$n should be > $min and < $max") {
        it in (min + 1) until max
    }
