package com.github.whyrising.y

import com.github.whyrising.y.map.IPersistentMap

interface IRef<T> : IDeref<T> {
    var validator: ((T) -> Boolean)?

    val watches: IPersistentMap<Any, (Any, IRef<T>, T, T) -> Any?>

    fun addWatch(key: Any, callback: (Any, IRef<T>, T, T) -> Any): IRef<T>

    fun removeWatch(key: Any): IRef<T>
}
