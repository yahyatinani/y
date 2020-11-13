package com.github.whyrising.y

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) {
    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap)
}
