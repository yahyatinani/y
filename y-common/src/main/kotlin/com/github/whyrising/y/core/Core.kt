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
