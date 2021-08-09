package com.github.whyrising.y

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalStdlibApi
class KeywordJvmTest : FreeSpec({
    beforeTest {
        keywordsCache.clear()
    }

    "concurrency" {
        val counter = atomic(0)

        withContext(Dispatchers.Default) {
            massiveRun2 {
                Keyword("${counter.incrementAndGet()}")
            }
        }

        counter.getAndSet(0)
        System.gc()

        withContext(Dispatchers.Default) {
            massiveRun2 {
                Keyword("${counter.incrementAndGet()}")
            }
        }

        keywordsCache.size shouldBeExactly 100000
    }

    "when gc collected a keyword, it should remove it from the cache" {
        Keyword("a")

        System.gc()

        Keyword("a")
    }
})

private suspend fun massiveRun2(action: suspend () -> Unit) {
    val n = 100
    val times = 1000
    coroutineScope {
        repeat(n) {
            launch { repeat(times) { action() } }
        }
    }
}
