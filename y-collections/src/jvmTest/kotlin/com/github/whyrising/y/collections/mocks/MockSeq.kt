package com.github.whyrising.y.collections.mocks

import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.Seqable
import com.github.whyrising.y.collections.seq.Sequential

class MockSeq<E>(private val v: PersistentVector<E>) : Seqable<E>, Sequential {
    override fun seq(): ISeq<E> = v.seq()
}
