package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) {
    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal class TransientHashSet<out E>(
        private
        val tmap: AtomicRef<ITransientMap<@UnsafeVariance E, @UnsafeVariance E>>
    ) : ConstantCount {

        override val count: Int
            get() = tmap.value.count
    }
}
