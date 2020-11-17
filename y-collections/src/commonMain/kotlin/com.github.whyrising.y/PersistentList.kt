package com.github.whyrising.y

import com.github.whyrising.y.PersistentList.Empty
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
sealed class PersistentList<out E> :
    ASeq<E>(), IPersistentList<E>, ConstantCount {

    @Suppress("UNCHECKED_CAST")
    internal class Cons<out E>(
        internal val first: E,
        internal val _rest: IPersistentList<E>
    ) : PersistentList<E>() {

        override fun first(): E = first

        override fun rest(): ISeq<E> = _rest as ISeq<E>

        override fun cons(e: @UnsafeVariance E): ISeq<E> = Cons(e, this)

        override val count: Int = _rest.count + 1

        override fun empty(): IPersistentCollection<E> = Empty

        override fun conj(e: @UnsafeVariance E): IPersistentCollection<E> =
            Cons(e, this)

        override fun peek(): E? = first

        override fun pop(): IPersistentStack<E> = _rest
    }

    internal abstract class AEmpty<out E> : PersistentList<E>() {

        override fun toString(): String = "()"

        @ExperimentalStdlibApi
        override fun hasheq(): Int = HASH_EQ

        override fun first(): E =
            throw NoSuchElementException("PersistentList is empty.")

        override fun rest(): ISeq<E> = this

        override fun cons(e: @UnsafeVariance E): ISeq<E> =
            Cons(e, this)

        override val count: Int = 0

        override fun empty(): AEmpty<E> = this

        override fun equiv(other: Any?): Boolean = equals(other)

        override fun conj(e: @UnsafeVariance E): PersistentList<E> =
            Cons(e, Empty)

        override fun seq(): ISeq<E> = this

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
            var l: PersistentList<E> = Empty

            val listIterator = list.listIterator(list.size)

            while (listIterator.hasPrevious())
                l = l.conj(listIterator.previous()) as PersistentList<E>

            return l
        }
    }
}

fun <E> l(): PersistentList<E> = Empty

fun <E> l(vararg elements: E): PersistentList<E> = PersistentList(*elements)

fun <E> List<E>.toPlist(): PersistentList<E> = PersistentList.create(this)
