package com.github.whyrising.y.concurrency.util

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun runAction(
    n: Int = 100,
    times: Int = 1000,
    action: suspend () -> Unit
) {
    coroutineScope {
        repeat(n) {
            launch { repeat(times) { action() } }
        }
    }
}
