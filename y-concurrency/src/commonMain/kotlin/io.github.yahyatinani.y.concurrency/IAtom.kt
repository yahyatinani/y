package io.github.yahyatinani.y.concurrency

interface IAtom<T> {
  fun reset(newValue: T): T

  fun swap(f: (currentVal: T) -> T): T

  fun <A> swap(arg: A, f: (currentVal: T, arg: A) -> T): T

  fun <A1, A2> swap(
    arg1: A1,
    arg2: A2,
    f: (currentVal: T, arg1: A1, arg2: A2) -> T,
  ): T

  fun compareAndSet(oldValue: T, newValue: T): Boolean
}
