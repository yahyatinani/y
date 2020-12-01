package com.github.whyrising.y.mocks

import com.github.whyrising.y.concretions.vector.PersistentVector
import com.github.whyrising.y.seq.ISeq
import com.github.whyrising.y.seq.Seqable
import com.github.whyrising.y.seq.Sequential

class MockSeq<E>(private val v: PersistentVector<E>) : Seqable<E>, Sequential {
    override fun seq(): ISeq<E> = v.seq()
}
