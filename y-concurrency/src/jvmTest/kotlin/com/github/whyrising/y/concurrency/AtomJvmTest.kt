package com.github.whyrising.y.concurrency

import com.github.whyrising.y.concurrency.util.runAction
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AtomJvmTest : FreeSpec({
    "swap(f)" - {
        """
            should loop over and over everytime the atom value doesn't match 
            the expected value due to other threads activities
        """{
            val atom = Atom(0)

            val coroutinesCount = 10
            val repeatCount = 10
            withContext(Dispatchers.Default) {
                runAction(coroutinesCount, repeatCount) {
                    atom.swap { currentVal ->
                        currentVal + 1
                    }
                }
            }

            atom.deref() shouldBeExactly coroutinesCount * repeatCount
        }
    }
})
