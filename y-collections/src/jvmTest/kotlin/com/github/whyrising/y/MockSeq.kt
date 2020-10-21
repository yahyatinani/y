package com.github.whyrising.y

class MockSeq<E>(private val v: PersistentVector<E>) : Seqable<E>, Sequential {
    override fun seq(): ISeq<E> = v.seq()
}
