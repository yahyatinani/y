package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.collections.PersistentList.Empty
import com.github.whyrising.y.core.seq
import com.github.whyrising.y.core.util.Murmur3
import com.github.whyrising.y.core.util.equiv
import com.github.whyrising.y.core.util.nth
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class LazySeq<out E> constructor(_f: () -> Any?) :
  SynchronizedObject(),
  ISeq<E>,
  List<E>,
  Sequence<E>,
  IHashEq,
  IPending,
  Sequential {
  internal var f: (() -> Any?)?
    private set

  internal var seq: ISeq<@UnsafeVariance E>
    private set

  internal var sVal: Any?
    private set

  init {
    f = _f
    seq = Empty
    sVal = null
  }

  internal fun seqVal(): Any {
    synchronized(this) {
      if (f != null) {
        sVal = f!!.invoke()
        f = null
      }

      if (sVal != null) {
        return sVal!!
      }

      return seq
    }
  }

  override fun seq(): ISeq<E> {
    synchronized(this) {
      seqVal()
      if (sVal != null) {
        var lazySeq: Any = sVal!!
        sVal = null

        while (lazySeq is LazySeq<*>)
          lazySeq = lazySeq.seqVal()

        // TODO: make seq nullable maybe?
        seq = (seq(lazySeq) ?: Empty) as ISeq<@UnsafeVariance E>
      }

      return seq
    }
  }

  override fun first(): E {
    seq()
    return seq.first()
  }

  override fun rest(): ISeq<E> {
    seq()
    return seq.rest()
  }

  override fun next(): ISeq<E>? {
    seq()
    if (seq is Empty) {
      return null
    }
    return seq.next()
  }

  override fun cons(e: @UnsafeVariance E): ISeq<E> = when (val s = seq()) {
    is Empty -> seq.cons(e)
    else -> Cons(e, s)
  }

  override val count: Int
    get() {
      var c = 0
      var s: ISeq<E>? = seq()
      while (s != null && s !is Empty) {
        ++c
        s = s.next()
      }

      return c
    }

  override fun empty(): IPersistentCollection<E> = Empty

  override fun equiv(other: Any?): Boolean = when (val s = seq()) {
    !is Empty -> s.equiv(other)
    else -> {
      val seq1 = seq(other)
      (other is Sequential || other is List<*>) &&
        (seq1 is Empty || seq1 == null)
    }
  }

  override fun conj(e: @UnsafeVariance E): ISeq<E> = cons(e)

  override fun hashCode(): Int = seq().hashCode()

  override fun equals(other: Any?): Boolean = when (val s = seq()) {
    !is Empty -> s == other
    else -> {
      val seq1 = seq(other)
      (other is Sequential || other is List<*>) &&
        (seq1 is Empty || seq1 == null)
    }
  }

  override fun hasheq(): Int = Murmur3.hashOrdered(this)

  override fun toString(): String = seq().toString()

  // list implementation
  override val size: Int
    get() = count

  override fun contains(element: @UnsafeVariance E): Boolean {
    var s: ISeq<E>? = seq()
    while (s != null) {
      if (equiv(s.first(), element)) {
        return true
      }
      s = s.next()
    }

    return false
  }

  override fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean {
    for (e in elements)
      if (!contains(e)) return false
    return true
  }

  override fun get(index: Int): E = nth(this, index)

  override fun indexOf(element: @UnsafeVariance E): Int {
    var s = seq()
    var i = 0
    while (s !is Empty) {
      if (equiv(s.first(), element)) return i
      i++
      s = s.rest()
    }

    return -1
  }

  override fun isEmpty(): Boolean = seq() is Empty

  override operator fun iterator(): Iterator<E> = SeqIterator(this)

  private fun reify(): List<E> = ArrayList(this)

  override fun lastIndexOf(element: @UnsafeVariance E): Int =
    reify().lastIndexOf(element)

  override fun listIterator(): ListIterator<E> = reify().listIterator()

  override fun listIterator(index: Int): ListIterator<E> =
    reify().listIterator(index)

  override fun subList(fromIndex: Int, toIndex: Int): List<E> =
    reify().subList(fromIndex, toIndex)

  override fun isRealized(): Boolean = synchronized(this) { return f == null }
}
