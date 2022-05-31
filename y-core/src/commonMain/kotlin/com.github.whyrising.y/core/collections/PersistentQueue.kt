package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.collections.PersistentList.Empty
import com.github.whyrising.y.core.collections.PersistentVector.EmptyVector
import com.github.whyrising.y.core.first
import com.github.whyrising.y.core.l
import com.github.whyrising.y.core.seq
import com.github.whyrising.y.core.util.Murmur3
import com.github.whyrising.y.core.util.count
import com.github.whyrising.y.core.util.equiv
import kotlinx.atomicfu.atomic
import kotlinx.serialization.Transient

class PersistentQueue<out E> private constructor(
  override val count: Int,
  val front: ISeq<E>,
  val back: PersistentVector<E>
) : IPersistentList<E>, Collection<E>, InstaCount, IHashEq {
  @Transient
  private var _hash by atomic(0)

  @Transient
  private var _hasheq by atomic(0)

  override fun toString(): String {
    var str = ""
    var s: ISeq<E>? = seq()
    while (s != null && s !is Empty) {
      if (str.isNotEmpty())
        str += " "
      str += "${s.first()}"
      s = s.next()
    }
    return "($str)"
  }

  override fun equiv(other: Any?): Boolean {
    if (other !is Sequential)
      return false

    var ms = seq<Any?>(other)
    var s: ISeq<E>? = seq(seq())
    while (s != null) {
      if (ms == null || !equiv(s.first(), ms.first()))
        return false

      s = s.next()
      ms = ms.next()
    }

    return ms == null
  }

  override fun equals(other: Any?): Boolean {
    if (other !is Sequential)
      return false

    var ms = seq<Any?>(other)
    var s: ISeq<E>? = seq(seq())
    while (s != null) {
      if (ms == null || s.first() != ms.first())
        return false

      s = s.next()
      ms = ms.next()
    }

    return ms == null
  }

  override fun hasheq(): Int {
    var cache = _hasheq
    if (cache == 0) {
      cache = Murmur3.hashOrdered(this)
      _hasheq = cache
    }
    return cache
  }

  override fun hashCode(): Int {
    if (_hash == 0) {
      var hash = 1
      var s: ISeq<E>? = seq()
      while (s != null && s !is Empty) {
        hash = 31 * hash + (s.first()?.hashCode() ?: 0)
        s = s.next()
      }
      _hash = hash
    }

    return _hash
  }

  override fun peek(): E? = first<E>(front)

  override fun pop(): PersistentQueue<E> = when {
    front is Empty || count == 1 -> EMPTY_QUEUE
    else -> {
      val newFront = front.rest()
      val c = count - 1
      when (newFront) {
        is Empty -> PersistentQueue(c, back.seq(), EmptyVector)
        else -> PersistentQueue(c, newFront, back)
      }
    }
  }

  override fun conj(e: @UnsafeVariance E): PersistentQueue<E> = when (front) {
    is Empty -> PersistentQueue(count + 1, l(e), EmptyVector)
    else -> PersistentQueue(count + 1, front, back.conj(e))
  }

  override fun empty(): IPersistentCollection<E> = EMPTY_QUEUE

  override fun seq(): ISeq<E> = when (front) {
    is Empty -> Empty
    else -> Seq(front, back.seq())
  }

  // Collection implementation

  override val size: Int
    get() = count

  override fun contains(element: @UnsafeVariance E): Boolean {
    var s: ISeq<E>? = seq()

    while (s != null && s !is Empty) {
      if (equiv(s.first(), element))
        return true
      s = s.next()
    }

    return false
  }

  override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
    for (e in elements)
      if (!contains(e)) return false
    return true
  }

  override fun isEmpty(): Boolean = count == 0

  override fun iterator(): Iterator<E> = object : Iterator<E> {
    private val backIter: Iterator<E> = back.iterator()
    private var fSeq: ISeq<E> = front

    override fun hasNext(): Boolean = fSeq !is Empty || backIter.hasNext()

    override fun next(): E = when {
      fSeq !is Empty -> {
        val first = fSeq.first()
        fSeq = fSeq.rest()
        first
      }
      backIter.hasNext() -> backIter.next()
      else -> throw NoSuchElementException()
    }
  }

  class Seq<E>(
    private val front: ISeq<E>,
    private val back: ISeq<E>
  ) : ASeq<E>() {
    override val count: Int
      get() = count(front) + count(back)

    override fun first(): E = front.first()

    override fun next(): ISeq<E>? = when (front) {
      is Empty -> null
      else -> {
        val nextF = front.next()
        when (back) {
          is Empty -> nextF
          else -> when (nextF) {
            null -> back
            else -> Seq(nextF, back)
          }
        }
      }
    }
  }

  companion object {
    private val EMPTY_QUEUE = PersistentQueue(0, Empty, EmptyVector)

    operator fun <E> invoke(): PersistentQueue<E> = EMPTY_QUEUE
  }
}
