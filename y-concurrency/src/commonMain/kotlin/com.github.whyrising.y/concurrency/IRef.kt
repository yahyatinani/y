package com.github.whyrising.y.concurrency

import com.github.whyrising.y.core.collections.IPersistentMap

interface IRef : IDeref {
  var validator: ((Any?) -> Boolean)?

  val watches: IPersistentMap<Any, (Any, IRef, Any?, Any?) -> Any?>

  fun addWatch(
    key: Any,
    callback: (key: Any, ref: IRef, oldVal: Any?, newVal: Any?) -> Any
  ): IRef

  fun removeWatch(key: Any): IRef
}
