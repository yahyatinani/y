package com.github.whyrising.y.mocks

import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.map.IMapEntry
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.stack.IPersistentStack
import com.github.whyrising.y.collections.vector.IPersistentVector

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

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun contains(element: @UnsafeVariance E): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(index: Int): E {
        TODO("Not yet implemented")
    }

    override fun indexOf(element: @UnsafeVariance E): Int {
        TODO("Not yet implemented")
    }

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<E> {
        TODO("Not yet implemented")
    }

    override fun lastIndexOf(element: @UnsafeVariance E): Int {
        TODO("Not yet implemented")
    }

    override fun listIterator(): ListIterator<E> {
        TODO("Not yet implemented")
    }

    override fun listIterator(index: Int): ListIterator<E> {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        TODO("Not yet implemented")
    }
}
