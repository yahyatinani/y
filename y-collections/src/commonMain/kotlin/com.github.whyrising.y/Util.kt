package com.github.whyrising.y

enum class Category {
    INTEGER,
    FLOATING
}

fun category(n: Number): Category = when (n) {
    is Byte -> Category.INTEGER
    is Short -> Category.INTEGER
    is Int -> Category.INTEGER
    is Long -> Category.INTEGER
    is Float -> Category.FLOATING
    is Double -> Category.FLOATING
    else -> throw IllegalStateException(
        "The category of the number: $n is not supported"
    )
}

fun ops(n: Number): Ops = when (n) {
    is Byte -> LongOps
    is Short -> LongOps
    is Int -> LongOps
    is Long -> LongOps
    is Float -> DoubleOps
    is Double -> DoubleOps
    else -> throw IllegalStateException(
        "The Ops of the number: $n is not supported"
    )
}

private fun sameCategory(x: Number, y: Number) = category(x) == category(y)

private fun areEquiv(x: Number, y: Number): Boolean =
    ops(x).combine(ops(y)).equiv(x, y)

fun <E> equiv(e1: E, e2: Any?): Boolean = when {
    e1 == e2 -> true
    e1 == null || e2 == null -> false
    e1 is Number && e2 is Number -> sameCategory(e1, e2) && areEquiv(e1, e2)
    else -> when {
        e1 is IPersistentCollection<*> -> e1.equiv(e2)
        e2 is IPersistentCollection<*> -> e2.equiv(e1)
        else -> false
    }
}
