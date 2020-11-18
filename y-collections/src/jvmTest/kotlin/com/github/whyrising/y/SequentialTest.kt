package com.github.whyrising.y

import com.github.whyrising.y.list.IPersistentList
import com.github.whyrising.y.seq.IndexedSeq
import com.github.whyrising.y.seq.Sequential
import com.github.whyrising.y.vector.IPersistentVector
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
