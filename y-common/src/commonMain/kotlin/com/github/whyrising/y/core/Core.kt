package com.github.whyrising.y.core

import kotlin.jvm.JvmName

fun <T> identity(x: T): T = x

class NotANumberError(x: Any) : RuntimeException("`$x` is not a number!")

inline fun <reified T> inc(x: Any): T = when (x) {
    is Byte -> x.inc() as T
    is Short -> x.inc() as T
    is Int -> x.inc() as T
    is Long -> x.inc() as T
    is Float -> x.inc() as T
    is Double -> x.inc() as T
// TODO is BigInteger -> x.inc() as T
    else -> throw NotANumberError(x)
}

fun dec(x: Byte): Byte = x.dec()

fun dec(x: Short): Short = x.dec()

fun dec(x: Int): Int = x.dec()

fun dec(x: Long): Long = x.dec()

// TODO
//fun dec(x: BigInteger): BigInteger = x.dec()

fun dec(x: Float): Float = x.dec()

fun dec(x: Double): Double = x.dec()

fun str(): String = ""

fun <T> str(x: T): String = when (x) {
    null -> ""
    else -> x.toString()
}

fun <T1, T2> str(x: T1, y: T2): String = "${str(x)}${str(y)}"

fun <T1, T2, T3> str(x: T1, y: T2, z: T3): String = "${str(x, y)}${str(z)}"

fun <T1, T2, T3, T> str(x: T1, y: T2, z: T3, vararg args: T): String =
    args.fold("") { acc, arg ->
        "$acc${str(arg)}"
    }.let { "${str(x, y, z)}$it" }

fun <T1, T2, R> curry(f: (T1, T2) -> R): (T1) -> (T2) -> R = { t1: T1 ->
    { t2: T2 ->
        f(t1, t2)
    }
}

fun <T1, T2, T3, R> curry(f: (T1, T2, T3) -> R): (T1) -> (T2) -> (T3) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 -> f(t1, t2, t3) }
        }
    }

fun <T1, T2, T3, T4, R> curry(
    f: (T1, T2, T3, T4) -> R
): (T1) -> (T2) -> (T3) -> (T4) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 ->
                { t4: T4 ->
                    f(t1, t2, t3, t4)
                }
            }
        }
    }

fun <T1, T2, T3, T4, T5, R> curry(
    f: (T1, T2, T3, T4, T5) -> R
): (T1) -> (T2) -> (T3) -> (T4) -> (T5) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 ->
                { t4: T4 ->
                    { t5: T5 ->
                        f(t1, t2, t3, t4, t5)
                    }
                }
            }
        }
    }

fun <T1, T2, T3, T4, T5, T6, R> curry(
    f: (T1, T2, T3, T4, T5, T6) -> R
): (T1) -> (T2) -> (T3) -> (T4) -> (T5) -> (T6) -> R =
    { t1: T1 ->
        { t2: T2 ->
            { t3: T3 ->
                { t4: T4 ->
                    { t5: T5 ->
                        { t6: T6 -> f(t1, t2, t3, t4, t5, t6) }
                    }
                }
            }
        }
    }

fun complement(f: () -> Boolean): () -> Boolean = { !f() }

fun <T> complement(f: (T) -> Boolean): (T) -> Boolean = { t: T ->
    !f(t)
}

@JvmName("complementY")
fun <T1, T2> complement(f: (T1) -> (T2) -> Boolean):
    (T1) -> (T2) -> Boolean = { t1: T1 -> { t2: T2 -> !f(t1)(t2) } }

@JvmName("complementY1")
fun <T1, T2, T3> complement(f: (T1) -> (T2) -> (T3) -> Boolean):
    (T1) -> (T2) -> (T3) -> Boolean = { t1: T1 ->
    { t2: T2 ->
        { t3: T3 ->
            !f(t1)(t2)(t3)
        }
    }
}

@JvmName("complementY2")
fun <T1, T2, T3, T4> complement(f: (T1) -> (T2) -> (T3) -> (T4) -> Boolean):
    (T1) -> (T2) -> (T3) -> (T4) -> Boolean = { t1: T1 ->
    { t2: T2 ->
        { t3: T3 ->
            { t4: T4 -> !f(t1)(t2)(t3)(t4) }
        }
    }
}

fun <T> compose(): (T) -> T = ::identity

fun <T> compose(f: T): T = f

fun <R2, R> compose(f: (R2) -> R, g: () -> R2): () -> R = { f(g()) }

fun <T1, R2, R> compose(f: (R2) -> R, g: (T1) -> R2): (T1) -> R =
    { t1: T1 -> f(g(t1)) }

@JvmName("composeY1")
fun <T1, T2, R2, R> compose(
    f: (R2) -> R,
    g: (T1) -> (T2) -> R2
): (T1) -> (T2) -> R = { t1: T1 -> { t2: T2 -> f(g(t1)(t2)) } }

@JvmName("composeY2")
fun <T1, T2, T3, R2, R> compose(
    f: (R2) -> R,
    g: (T1) -> (T2) -> (T3) -> R2
): (T1) -> (T2) -> (T3) -> R = { t1: T1 ->
    { t2: T2 ->
        { t3: T3 ->
            f(g(t1)(t2)(t3))
        }
    }
}
