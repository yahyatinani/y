package com.github.whyrising.y.concurrency

import com.github.whyrising.y.core.collections.IPersistentMap
import com.github.whyrising.y.core.collections.ISeq
import com.github.whyrising.y.core.collections.MapEntry
import com.github.whyrising.y.core.collections.PersistentHashMap
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

abstract class ARef : IRef {
  private val lock = reentrantLock()
  private val _validator: AtomicRef<((Any?) -> Boolean)?> = atomic(null)

  private
  val _watches = atomic<IPersistentMap<Any, (Any, IRef, Any?, Any?) -> Any>>(
    PersistentHashMap.EmptyHashMap
  )

  private fun validate(vf: ((Any?) -> Boolean)?, value: Any?) {
    if (vf == null) return

    fun invalidReferenceState(cause: Throwable? = null) =
      IllegalStateException("Invalid reference state", cause)

    val isValid = try {
      vf(value)
    } catch (e: Exception) {
      throw invalidReferenceState(e)
    }

    if (!isValid) {
      throw invalidReferenceState()
    }
  }

  fun validate(value: Any?) {
    validate(_validator.value, value)
  }

  override var validator: ((Any?) -> Boolean)?
    get() = _validator.value
    set(vf) {
      validate(vf, deref())
      _validator.value = vf
    }

  override val watches: IPersistentMap<Any, (Any, IRef, Any?, Any?) -> Any?> by
  _watches

  override fun addWatch(
    key: Any,
    callback: (Any, IRef, Any?, Any?) -> Any
  ): IRef {
    lock.withLock {
      _watches.value = _watches.value.assoc(key, callback)
      return this
    }
  }

  override fun removeWatch(key: Any): IRef {
    lock.withLock {
      _watches.value = _watches.value.dissoc(key)
      return this
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun notifyWatches(oldVal: Any?, newVal: Any?) {
    val ws = _watches.value
    if (ws.count <= 0) return

    var s = ws.seq() as ISeq<MapEntry<Any, (Any, IRef, Any?, Any?) -> Any>>?
    while (s != null) {
      val e = s.first()
      val f = e.value
      f(e.key, this, oldVal, newVal)

      s = s.next()
    }
  }
}
