package com.github.whyrising.y.core

import java.math.BigInteger

fun <T> identity(x: T): T = x

fun inc(x: Byte): Byte = x.inc()

fun inc(x: Short): Short = x.inc()

fun inc(x: Int): Int = x.inc()

fun inc(x: Long): Long = x.inc()

fun inc(x: BigInteger): BigInteger = x.inc()

fun inc(x: Float): Float = x.inc()

fun inc(x: Double): Double = x.inc()

fun dec(x: Byte): Byte = x.dec()

fun dec(x: Short): Short = x.dec()

fun dec(x: Int): Int = x.dec()

fun dec(x: Long): Long = x.dec()

fun dec(x: BigInteger): BigInteger = x.dec()

fun dec(x: Float): Float = x.dec()

fun dec(x: Double): Double = x.dec()

fun str(): String = ""

fun <T> str(vararg xs: T): String = xs.fold("") { acc, x ->
    when (x) {
        null -> acc
        else -> "$acc$x"
    }
}

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
