package com.github.whyrising.y.collections.mocks

import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.map.IMapEntry
import com.github.whyrising.y.collections.map.IPersistentMap
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq

class MockPersistentMap<K, V> private constructor(
    private val map: PersistentArrayMap<K, V>
) : IPersistentMap<K, V>, Map<K, V> {

    @ExperimentalStdlibApi
    override fun assoc(key: K, value: V): IPersistentMap<K, V> =
        map.assoc(key, value)

    @ExperimentalStdlibApi
    override fun assocNew(key: K, value: V): IPersistentMap<K, V> =
        map.assocNew(key, value)

    override fun dissoc(key: K): IPersistentMap<K, V> = map.dissoc(key)

    override fun containsKey(key: K): Boolean = map.containsKey(key)

    override fun entryAt(key: K): IMapEntry<K, V>? = map.entryAt(key)

    override fun valAt(key: K, default: V?): V? = map.valAt(key, default)

    override fun valAt(key: K): V? = map.valAt(key)

    override val count: Int = map.count

    override fun empty(): IPersistentCollection<Any?> = map.empty()

    override fun seq(): ISeq<Any?> = map.seq()

    override fun equiv(other: Any?): Boolean = map.equiv(other)

    override fun conj(e: Any?): IPersistentCollection<Any?> = map.conj(e)

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator
        fun <K, V> invoke(vararg pairs: Pair<K, V>): MockPersistentMap<K, V> =
            MockPersistentMap(PersistentArrayMap.createWithCheck(*pairs))
    }

    override val entries: Set<Map.Entry<K, V>>
        get() = TODO("Not yet implemented")
    override val keys: Set<K>
        get() = TODO("Not yet implemented")
    override val size: Int
        get() = count
    override val values: Collection<V>
        get() = TODO("Not yet implemented")

    override fun containsValue(value: V): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: K): V? = map[key]

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<Map.Entry<K, V>> = map.iterator()

    override fun keyz(): ISeq<K> {
        TODO("Not yet implemented")
    }

    override fun vals(): ISeq<V> {
        TODO("Not yet implemented")
    }
}
