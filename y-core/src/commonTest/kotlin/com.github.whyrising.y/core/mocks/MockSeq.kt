package com.github.whyrising.y.core.mocks

import com.github.whyrising.y.core.collections.IPersistentVector
import com.github.whyrising.y.core.collections.ISeq
import com.github.whyrising.y.core.collections.Seqable
import com.github.whyrising.y.core.collections.Sequential

class MockSeq<E>(private val v: IPersistentVector<E>) : Seqable<E>, Sequential {
  override fun seq(): ISeq<E> = v.seq()
}
