package com.github.whyrising.y.mocks

import com.github.whyrising.y.ISeq
import com.github.whyrising.y.PersistentVector
import com.github.whyrising.y.Seqable
import com.github.whyrising.y.Sequential

class MockSeq<E>(private val v: PersistentVector<E>) : Seqable<E>, Sequential {
    override fun seq(): ISeq<E> = v.seq()
}
