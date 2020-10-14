package com.github.whyrising.y


sealed class PersistentVector<out E>(
    internal val count: Int,
    internal val shift: Int,
    internal val root: Node<E>,
    internal val tail: Array<Any?>
) {

    internal class Node<out T>(val array: Array<Any?>)

    internal abstract class AEmptyPersistentVector<E> : PersistentVector<E>(
        0,
        5,
        Node(arrayOfNulls(32)),
        arrayOfNulls(0)
    ) {
        override fun toString(): String = "[]"
    }

    internal object EmptyPersistentVector : AEmptyPersistentVector<Nothing>()

    companion object {
        operator fun <E> invoke(): PersistentVector<E> = EmptyPersistentVector
    }
}
