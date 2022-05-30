package com.github.whyrising.y.concurrency

import com.github.whyrising.y.core.collections.IPersistentMap

interface IRef<T> : IDeref<T> {
    var validator: ((T) -> Boolean)?

    val watches: IPersistentMap<Any, (Any, IRef<T>, T, T) -> Any?>

    fun addWatch(
        key: Any,
        callback: (key: Any, ref: IRef<T>, oldVal: T, newVal: T) -> Any
    ): IRef<T>

    fun removeWatch(key: Any): IRef<T>
}
