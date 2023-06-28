package io.github.yahyatinani.y.concurrency

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

class Atom internal constructor(x: Any?) : ARef(), IAtom2 {
  private val _state: AtomicRef<Any?> = atomic(x)

  internal val state by _state

  @Suppress("UNCHECKED_CAST")
  override fun <T> deref(): T = state as T

  override fun <T> swap(f: (currentVal: T) -> Any?): Any? {
    while (true) {
      val currentV = deref<T>()
      val newVal = f(currentV)
      validate(newVal)

      if (_state.compareAndSet(currentV, newVal)) {
        notifyWatches(currentV, newVal)
        return newVal
      }
    }
  }

  override fun <A> swap(arg: A, f: (currentVal: Any?, arg: A) -> Any?): Any? {
    while (true) {
      val currentV = _state.value
      val newVal = f(currentV, arg)
      validate(newVal)

      if (_state.compareAndSet(currentV, newVal)) {
        notifyWatches(currentV, newVal)
        return newVal
      }
    }
  }

  override fun <A1, A2> swap(
    arg1: A1,
    arg2: A2,
    f: (currentVal: Any?, arg1: A1, arg2: A2) -> Any?,
  ): Any? {
    while (true) {
      val currentV = _state.value
      val newVal = f(currentV, arg1, arg2)
      validate(newVal)

      if (_state.compareAndSet(currentV, newVal)) {
        notifyWatches(currentV, newVal)
        return newVal
      }
    }
  }

  override fun <V> swapVals(f: (currentVal: V) -> V): Pair<V, V> {
    while (true) {
      val oldValue = deref<V>()
      val newVal = f(oldValue)
      validate(newVal)

      if (_state.compareAndSet(oldValue, newVal)) {
        notifyWatches(oldValue, newVal)
        return oldValue to newVal
      }
    }
  }

  override fun <A> swapVals(
    arg: A,
    f: (currentVal: Any?, arg: A) -> Any?,
  ): Pair<Any?, Any?> {
    while (true) {
      val oldValue = _state.value
      val newVal = f(oldValue, arg)
      validate(newVal)

      if (_state.compareAndSet(oldValue, newVal)) {
        notifyWatches(oldValue, newVal)
        return oldValue to newVal
      }
    }
  }

  override fun <A1, A2> swapVals(
    arg1: A1,
    arg2: A2,
    f: (currentVal: Any?, arg1: A1, arg2: A2) -> Any?,
  ): Pair<Any?, Any?> {
    while (true) {
      val oldValue = _state.value
      val newVal = f(oldValue, arg1, arg2)
      validate(newVal)

      if (_state.compareAndSet(oldValue, newVal)) {
        notifyWatches(oldValue, newVal)
        return oldValue to newVal
      }
    }
  }

  override fun reset(newValue: Any?): Any? {
    val oldValue = _state.value
    validate(newValue)
    _state.value = newValue
    notifyWatches(oldValue, newValue)
    return newValue
  }

  /**
   * Atomically sets the value of [Atom] to [newValue] if and only if the
   * current value of the atom is identical (===) to [oldValue].
   *
   * @return true only if set happened, otherwise, false.
   */
  override fun compareAndSet(oldValue: Any?, newValue: Any?): Boolean {
    validate(newValue)
    val ret = _state.compareAndSet(oldValue, newValue)
    if (ret) notifyWatches(oldValue, newValue)
    return ret
  }

  override fun resetVals(newValue: Any?): Pair<Any?, Any?> {
    validate(newValue)
    val oldValue = _state.getAndSet(newValue)
    notifyWatches(oldValue, newValue)
    return oldValue to newValue
  }

  /** @return the value of the atom by calling deref() */
  operator fun invoke(): Any? = deref()
}

/**
 * @param x an initial value to be hold by the atom.
 * @return an `Atom<T>` with an initial value x.
 */
fun atom(x: Any?): Atom = Atom(x)
