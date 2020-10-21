package com.github.whyrising.y

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.reflection.shouldBeSubtypeOf

class SequentialTest : FreeSpec({
    "assert Sequential" {
        IPersistentList::class.shouldBeSubtypeOf<Sequential>()
        IPersistentVector::class.shouldBeSubtypeOf<Sequential>()
        ASeq::class.shouldBeSubtypeOf<Sequential>()
        IndexedSeq::class.shouldBeSubtypeOf<Sequential>()
    }
})
