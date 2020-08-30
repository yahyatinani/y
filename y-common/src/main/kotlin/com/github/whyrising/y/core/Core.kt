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
