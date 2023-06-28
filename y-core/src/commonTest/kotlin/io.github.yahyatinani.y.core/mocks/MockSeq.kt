package io.github.yahyatinani.y.core.mocks

import io.github.yahyatinani.y.core.collections.IPersistentVector
import io.github.yahyatinani.y.core.collections.ISeq
import io.github.yahyatinani.y.core.collections.Seqable
import io.github.yahyatinani.y.core.collections.Sequential

class MockSeq<E>(private val v: IPersistentVector<E>) : Seqable<E>, Sequential {
  override fun seq(): ISeq<E> = v.seq()
}
