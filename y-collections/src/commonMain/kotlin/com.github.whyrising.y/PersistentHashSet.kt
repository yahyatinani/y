package com.github.whyrising.y

sealed class PersistentHashSet<out E>(val map: IPersistentMap<E, E>) {
    object EmptyHashSet : PersistentHashSet<Nothing>(LeanMap.EmptyLeanMap)

    internal class TransientHashSet<out E>(
        val tmap: ITransientMap<E, E>
    ) : ITransientMap<Int, Int> {

        override fun assoc(key: Int, value: Int): ITransientMap<Int, Int> {
            TODO("Not yet implemented")
        }

        override fun dissoc(key: Int): ITransientMap<Int, Int> {
            TODO("Not yet implemented")
        }

        override fun persistent(): IPersistentMap<Int, Int> {
            TODO("Not yet implemented")
        }

        override fun conj(e: Any?): ITransientCollection<Any?> {
            TODO("Not yet implemented")
        }

        override fun valAt(key: Int, default: Int?): Int? {
            TODO("Not yet implemented")
        }

        override fun valAt(key: Int): Int? {
            TODO("Not yet implemented")
        }

        override val count: Int
            get() = TODO("Not yet implemented")
    }
}
