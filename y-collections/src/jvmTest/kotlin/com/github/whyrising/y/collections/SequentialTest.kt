package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.ASeq
import com.github.whyrising.y.collections.list.IPersistentList
import com.github.whyrising.y.collections.seq.IndexedSeq
import com.github.whyrising.y.collections.seq.Sequential
import com.github.whyrising.y.collections.vector.IPersistentVector
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
