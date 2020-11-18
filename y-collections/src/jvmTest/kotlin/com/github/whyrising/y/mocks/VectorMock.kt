package com.github.whyrising.y.mocks

import com.github.whyrising.y.IMapEntry
import com.github.whyrising.y.IPersistentCollection
import com.github.whyrising.y.IPersistentStack
import com.github.whyrising.y.IPersistentVector
import com.github.whyrising.y.ISeq
import com.github.whyrising.y.PersistentVector

class VectorMock<out E>(val vec: PersistentVector<E>) : IPersistentVector<E> {

    override fun length(): Int {
        TODO("Not yet implemented")
    }

    override
    fun assocN(index: Int, value: @UnsafeVariance E): IPersistentVector<E> {
        TODO("Not yet implemented")
    }

    override fun conj(e: @UnsafeVariance E): IPersistentVector<E> {
        TODO("Not yet implemented")
    }

    override fun subvec(start: Int, end: Int): IPersistentVector<E> {
        TODO("Not yet implemented")
    }

    override fun containsKey(key: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun entryAt(key: Int): IMapEntry<Int, E>? {
        TODO("Not yet implemented")
    }

    override
    fun assoc(key: Int, value: @UnsafeVariance E): IPersistentVector<E> {
        TODO("Not yet implemented")
    }

    override fun valAt(key: Int, default: @UnsafeVariance E?): E? {
        TODO("Not yet implemented")
    }

    override fun valAt(key: Int): E? {
        TODO("Not yet implemented")
    }

    override val count: Int = vec.count

    override fun empty(): IPersistentCollection<E> {
        TODO("Not yet implemented")
    }

    override fun equiv(other: Any?): Boolean {
        TODO("Not yet implemented")
    }

    override fun seq(): ISeq<E> {
        TODO("Not yet implemented")
    }

    override fun nth(index: Int): E = vec.nth(index)

    override fun nth(index: Int, default: @UnsafeVariance E): E {
        TODO("Not yet implemented")
    }

    override fun peek(): E? {
        TODO("Not yet implemented")
    }

    override fun pop(): IPersistentStack<E> {
        TODO("Not yet implemented")
    }
}
