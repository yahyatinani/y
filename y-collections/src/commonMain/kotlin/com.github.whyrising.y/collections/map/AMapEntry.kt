package com.github.whyrising.y.collections.map

import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.EmptyVector
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.stack.IPersistentStack
import com.github.whyrising.y.collections.vector.APersistentVector
import com.github.whyrising.y.collections.vector.IPersistentVector

abstract class AMapEntry<out K, out V> :
    APersistentVector<Any?>(), IMapEntry<K, V> {

    override fun nth(index: Int): Any? = when (index) {
        0 -> key
        1 -> value
        else -> throw IndexOutOfBoundsException("index = $index")
    }

    private fun toVector(): IPersistentVector<Any?> =
        PersistentVector(key, value)

    override fun assocN(index: Int, value: Any?): IPersistentVector<Any?> =
        toVector().assocN(index, value)

    override fun conj(e: Any?): IPersistentVector<Any?> =
        toVector().conj(e)

    override val count: Int
        get() = 2

    override fun empty(): IPersistentCollection<Any?> = EmptyVector

    override fun pop(): IPersistentStack<Any?> = PersistentVector(key)
}
