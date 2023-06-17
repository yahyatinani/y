package com.github.whyrising.y.concurrency

interface IAtom {
  fun reset(newValue: Any?): Any?

  fun <T> swap(f: (currentVal: T) -> Any?): Any?

  fun <A> swap(arg: A, f: (currentVal: Any?, arg: A) -> Any?): Any?

  fun <A1, A2> swap(
    arg1: A1,
    arg2: A2,
    f: (currentVal: Any?, arg1: A1, arg2: A2) -> Any?,
  ): Any?

  fun compareAndSet(oldValue: Any?, newValue: Any?): Boolean
}
