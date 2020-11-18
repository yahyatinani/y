package com.github.whyrising.y

import com.github.whyrising.y.PersistentVector.EmptyVector
import com.github.whyrising.y.seq.IPersistentCollection
import com.github.whyrising.y.map.IMapEntry
import com.github.whyrising.y.stack.IPersistentStack
import com.github.whyrising.y.vector.IPersistentVector

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
