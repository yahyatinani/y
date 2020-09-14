package com.github.whyrising.y.values

fun <T> assertCondition(value: T, msg: String, p: (T) -> Boolean): Result<T> =
    Result.of(value, msg, p)

fun <T> assertCondition(value: T, p: (T) -> Boolean): Result<T> =
    Result.of(value, "Assertion error: condition should be true", p)
