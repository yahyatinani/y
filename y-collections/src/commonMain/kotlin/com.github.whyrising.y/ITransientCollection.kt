package com.github.whyrising.y

interface ITransientCollection<E> {
    fun persistent(): PersistentVector<E> //TODO: return IPersistentCollection
}
