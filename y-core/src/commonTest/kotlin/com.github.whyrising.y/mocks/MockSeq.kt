package com.github.whyrising.y.mocks

import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.Seqable
import com.github.whyrising.y.collections.seq.Sequential
import com.github.whyrising.y.collections.vector.IPersistentVector

class MockSeq<E>(private val v: IPersistentVector<E>) : Seqable<E>, Sequential {
    override fun seq(): ISeq<E> = v.seq()
}
