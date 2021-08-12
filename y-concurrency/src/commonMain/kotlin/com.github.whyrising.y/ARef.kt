package com.github.whyrising.y

import com.github.whyrising.y.concretions.map.MapEntry
import com.github.whyrising.y.concretions.map.PersistentHashMap
import com.github.whyrising.y.map.IPersistentMap
import com.github.whyrising.y.seq.ISeq
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

abstract class ARef<T> : IRef<T> {
    private val lock = reentrantLock()
    private val _validator: AtomicRef<((T) -> Boolean)?> = atomic(null)

    private
    val _watches = atomic<IPersistentMap<Any, (Any, IRef<T>, T, T) -> Any>>(
        PersistentHashMap.EmptyHashMap
    )

    private fun validate(vf: ((T) -> Boolean)?, value: T) {
        if (vf == null) return

        fun invalidReferenceState(cause: Throwable? = null) =
            IllegalStateException("Invalid reference state", cause)

        val isValid = try {
            vf(value)
        } catch (e: Exception) {
            throw invalidReferenceState(e)
        }

        if (!isValid)
            throw invalidReferenceState()
    }

    fun validate(value: T) {
        validate(_validator.value, value)
    }

    override var validator: ((T) -> Boolean)?
        get() = _validator.value
        set(vf) {
            validate(vf, deref())
            _validator.value = vf
        }

    override val watches: IPersistentMap<Any, (Any, IRef<T>, T, T) -> Any?> by
    _watches

    override fun addWatch(
        key: Any,
        callback: (Any, IRef<T>, T, T) -> Any
    ): IRef<T> {
        lock.withLock {
            _watches.value = _watches.value.assoc(key, callback)
            return this
        }
    }

    override fun removeWatch(key: Any): IRef<T> {
        lock.withLock {
            _watches.value = _watches.value.dissoc(key)
            return this
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun notifyWatches(oldVal: T, newVal: T) {
        val ws = _watches.value
        if (ws.count > 0) {
            var s = ws.seq() as ISeq<MapEntry<Any, (Any, IRef<T>, T, T) -> Any>>
            val emptySeq = s.empty()
            while (s != emptySeq) {
                val e = s.first()
                val f = e.value
                f(e.key, this, oldVal, newVal)
                
                s = s.rest()
            }
        }
    }
}
