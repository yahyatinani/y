package io.github.yahyatinani.y.core

import io.kotest.matchers.ints.shouldBeExactly
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun runAction(
  n: Int = 100,
  times: Int = 1000,
  action: suspend () -> Unit,
) {
  coroutineScope {
    repeat(n) {
      launch { repeat(times) { action() } }
    }
  }
}

fun assertArraysAreEquiv(a1: Array<Any?>, a2: Array<Any?>) {
  a2.fold(0) { index: Int, i: Any? ->
    val n = a1[index] as Int

    n shouldBeExactly i as Int

    index + 1
  }
}
