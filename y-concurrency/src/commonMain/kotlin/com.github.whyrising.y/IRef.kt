package com.github.whyrising.y

interface IRef<T> : IDeref<T> {
    var validator: ((T) -> Boolean)?
}
