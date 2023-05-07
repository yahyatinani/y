package com.github.whyrising.y.concurrency

interface IAtom2 : IAtom {
  fun <V> swapVals(f: (currentVal: V) -> V): Pair<V, V>

  fun <A> swapVals(
    arg: A,
    f: (currentVal: Any?, arg: A) -> Any?
  ): Pair<Any?, Any?>

  fun <A1, A2> swapVals(
    arg1: A1,
    arg2: A2,
    f: (currentVal: Any?, arg1: A1, arg2: A2) -> Any?
  ): Pair<Any?, Any?>

  fun resetVals(newValue: Any?): Pair<Any?, Any?>
}
