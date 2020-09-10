package com.github.whyrising.y.values

sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    abstract fun <U> map(f: (T) -> U): Option<U>

    abstract fun <U> flatMap(f: (T) -> Option<U>): Option<U>

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Some -> value
        None -> default()
    }

    fun orElse(default: () -> Option<@UnsafeVariance T>): Option<T> =
        map { this }.getOrElse(default)

    fun filter(p: (T) -> Boolean): Option<T> =
        flatMap { if (p(it)) this else None }

    internal object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun <U> map(f: (Nothing) -> U): Option<U> = None

        override fun <U> flatMap(f: (Nothing) -> Option<U>): Option<U> = None

        override fun toString(): String = "None"

        override fun hashCode(): Int = 0
    }

    internal data class Some<out T>(internal val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false

        private fun <U> some(f: (T) -> U) = Some(f(value))

        override fun <U> map(f: (T) -> U): Option<U> = some(f)

        override fun <U> flatMap(f: (T) -> Option<U>): Option<U> = some(f).value
    }

    companion object {
        operator fun <T> invoke(x: T? = null): Option<T> = when (x) {
            null -> None
            else -> Some(x)
        }
    }
}

fun <T, R> lift(f: (T) -> R): (Option<T>) -> Option<R> = { it.map(f) }

@JvmName("lift1")
fun <T1, T2, R> lift(
    f: (T1) -> (T2) -> R
): (Option<T1>) -> (Option<T2>) -> Option<R> = { option1: Option<T1> ->
    { option2: Option<T2> ->
        option1.flatMap { t1 ->
            option2.map { t2: T2 ->
                f(t1)(t2)
            }
        }
    }
}


@JvmName("lift2")
fun <T1, T2, T3, R> lift(
    f: (T1) -> (T2) -> (T3) -> R
): (Option<T1>) -> (Option<T2>) -> (Option<T3>) -> Option<R> = { option1 ->
    { option2 ->
        { option3 ->
            option1.flatMap { t1 ->
                option2.flatMap { t2 ->
                    option3.map { t3 ->
                        f(t1)(t2)(t3)
                    }
                }
            }
        }
    }
}

fun <T, R> hLift(f: (T) -> R): (T) -> Option<R> = { Option(it).map(f) }

@JvmName("hLift1")
fun <T1, T2, R> hLift(f: (T1) -> (T2) -> R): (T1) -> (T2) -> Option<R> =
    { t1 ->
        { t2 ->
            Option(f(t1)).flatMap { f1 ->
                Option(t2).map(f1)
            }
        }
    }

@JvmName("hLift2")
fun <T1, T2, T3, R> hLift(
    f: (T1) -> (T2) -> (T3) -> R
): (T1) -> (T2) -> (T3) -> Option<R> = { t1: T1 ->
    { t2 ->
        { t3 ->
            Option(f(t1)).flatMap { f1 ->
                Option(f1(t2)).flatMap { f2 ->
                    Option(t3).map(f2)
                }
            }
        }
    }
}

fun <T1, T2, R> map(
    op1: Option<T1>,
    op2: Option<T2>,
    f: (T1) -> (T2) -> R
): Option<R> = op1.flatMap { t1 -> op2.map { t2 -> f(t1)(t2) } }

fun <T1, T2, T3, R> map(
    op1: Option<T1>,
    op2: Option<T2>,
    op3: Option<T3>,
    f: (T1) -> (T2) -> (T3) -> R
): Option<R> = op1.flatMap { t1 ->
    op2.flatMap { t2: T2 ->
        op3.map { t3 -> f(t1)(t2)(t3) }
    }
}
