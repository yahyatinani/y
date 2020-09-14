package com.github.whyrising.y.values

fun <T> assertCondition(value: T, msg: String, p: (T) -> Boolean): Result<T> =
    Result.of(value, msg, p)

fun <T> assertCondition(value: T, p: (T) -> Boolean): Result<T> =
    Result.of(value, "Assertion error: condition should be true", p)

fun assertTrue(
    condition: Boolean,
    errMsg: String = "Assertion error: condition should be true"
): Result<Boolean> =
    assertCondition(condition, errMsg) { it }
