package com.github.whyrising.y.collections.concretions.list

import com.github.whyrising.y.collections.core.InstaCount
import com.github.whyrising.y.collections.core.toPlist
import com.github.whyrising.y.collections.list.IPersistentList
import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.stack.IPersistentStack
import com.github.whyrising.y.collections.util.Murmur3
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class PersistentListSerializer<E>(element: KSerializer<E>) :
    KSerializer<PersistentList<E>> {

    internal val listSerializer = ListSerializer(element)

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): PersistentList<E> =
        listSerializer.deserialize(decoder).toPlist()

    override fun serialize(encoder: Encoder, value: PersistentList<E>) =
        listSerializer.serialize(encoder, value)
}

@Serializable(with = PersistentListSerializer::class)
sealed class PersistentList<out E> : ASeq<E>(), IPersistentList<E>, InstaCount {

    override fun conj(e: @UnsafeVariance E): PersistentList<E> = Cons(e, this)

    override fun cons(e: @UnsafeVariance E): ISeq<E> = Cons(e, this)

    @Suppress("UNCHECKED_CAST")
    internal class Cons<out E>(
        internal val first: E,
        internal val rest: IPersistentList<E>
    ) : PersistentList<E>() {

        override fun first(): E = first

        override fun rest(): ISeq<E> = rest as ISeq<E>

        override val count: Int = rest.count + 1

        override fun empty(): IPersistentCollection<E> = Empty

        override fun peek(): E = first

        override fun pop(): IPersistentStack<E> = rest
    }

    internal abstract class AEmpty<out E> : PersistentList<E>() {

        override fun toString(): String = "()"

        @ExperimentalStdlibApi
        override fun hasheq(): Int = HASH_EQ

        override fun first(): E =
            throw NoSuchElementException("PersistentList is empty.")

        override fun rest(): ISeq<E> = this

        override val count: Int = 0

        override fun empty(): AEmpty<E> = this

        override fun equiv(other: Any?): Boolean = equals(other)

        override fun peek(): E? = null

        override fun pop(): IPersistentStack<E> = this

        // List implementation
        override val size: Int = 0

        override
        fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean =
            elements.isEmpty()

        override fun get(index: Int): E =
            throw IndexOutOfBoundsException("Can't call get on an empty list")

        override fun isEmpty(): Boolean = true

        override fun iterator(): Iterator<E> = object : Iterator<E> {
            override fun hasNext(): Boolean = false

            override fun next(): E = throw NoSuchElementException()
        }

        override fun listIterator(): ListIterator<E> =
            listOf<E>().listIterator()

        override fun listIterator(index: Int): ListIterator<E> =
            listOf<E>().listIterator(index)

        override fun subList(fromIndex: Int, toIndex: Int): List<E> =
            listOf<E>().subList(fromIndex, toIndex)

        override fun contains(element: @UnsafeVariance E): Boolean {
            TODO("Not yet implemented")
        }

        override fun indexOf(element: @UnsafeVariance E): Int {
            TODO("Not yet implemented")
        }

        override fun lastIndexOf(element: @UnsafeVariance E): Int {
            TODO("Not yet implemented")
        }

        companion object {
            @ExperimentalStdlibApi
            private val HASH_EQ = Murmur3.hashOrdered(emptyList<Nothing>())
        }
    }

    internal object Empty : AEmpty<Nothing>()

    companion object {
        internal operator fun <E> invoke(): PersistentList<E> = Empty

        internal operator fun <E> invoke(vararg args: E): PersistentList<E> =
            args.foldRight(Empty) { e: E, acc: IPersistentList<E> ->
                Cons(e, acc)
            }

        internal fun <E> create(list: List<E>): PersistentList<E> {
            val listIterator = list.listIterator(list.size)

            var l: PersistentList<E> = Empty
            while (listIterator.hasPrevious())
                l = l.conj(listIterator.previous())

            return l
        }
    }
}
