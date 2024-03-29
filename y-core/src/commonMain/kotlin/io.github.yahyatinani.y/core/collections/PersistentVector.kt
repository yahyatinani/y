package io.github.yahyatinani.y.core.collections

import io.github.yahyatinani.y.core.collections.PersistentVector.Node.EmptyNode
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.serialization.Serializable

internal const val SHIFT = 5
internal const val BF = 32

@Serializable(with = PersistentVectorSerializer::class)
sealed class PersistentVector<out E>(
  override val count: Int,
  internal val shift: Int,
  internal val root: Node<E>,
  internal val tail: Array<Any?>,
) : APersistentVector<E>(), IMutableCollection<E> {

  override fun assocN(
    index: Int,
    value: @UnsafeVariance E,
  ): IPersistentVector<E> {
    @Suppress("UNCHECKED_CAST")
    fun assoc(level: Int, node: Node<E>): Node<E> {
      val copy: Node<E> = Node(node.edit, node.array.copyOf())

      when (level) {
        0 -> copy.array[index and 0x01f] = value
        else -> {
          val subIndex = (index ushr level) and 0x01f
          copy.array[subIndex] =
            assoc(level - SHIFT, (node.array[subIndex] as Node<E>))
        }
      }

      return copy
    }

    return when (index) {
      in 0 until count -> {
        when {
          index >= tailOffset(count) -> {
            val newTail = tail.copyOf()
            newTail[index and 0x01f] = value

            Vector(count, shift, root, newTail)
          }
          else -> Vector(count, shift, assoc(shift, root), tail)
        }
      }
      count -> conj(value)
      else -> throw IndexOutOfBoundsException("index = $index")
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun pushTail(level: Int, parent: Node<E>, tail: Node<E>): Node<E> {
    val rootNode = Node<E>(parent.edit, parent.array.copyOf())
    val subIndex = ((count - 1) ushr level) and 0x01f

    val nodeToInsert: Node<E> = if (level == SHIFT) {
      tail
    } else {
      when (val child = parent.array[subIndex]) {
        null -> newPath(root.edit, level - 5, tail)
        else -> pushTail(level - SHIFT, child as Node<E>, tail)
      }
    }

    rootNode.array[subIndex] = nodeToInsert

    return rootNode
  }

  override fun conj(e: @UnsafeVariance E): PersistentVector<E> {
    // empty slot available in tail?
    if (count - tailOffset(count) < BF) {
      val newTail = tail.copyOf(tail.size + 1)
      newTail[tail.size] = e

      return Vector(count + 1, shift, root, newTail)
    }

    val tailNode = Node<E>(root.edit, tail)
    var newShift = shift
    val newRoot: Node<E>

    when {
      // root overflow?
      (count ushr SHIFT) > (1 shl shift) -> {
        newRoot = Node(root.edit)
        newRoot.array[0] = root
        newRoot.array[1] = newPath(root.edit, shift, tailNode)
        newShift += SHIFT
      }
      else -> newRoot = pushTail(shift, root, tailNode)
    }

    return Vector(count + 1, newShift, newRoot, arrayOf(e))
  }

  override fun empty(): IPersistentCollection<E> = EmptyVector

  @Suppress("UNCHECKED_CAST")
  internal fun leafArrayBy(index: Int): Array<Any?> = when {
    indexOutOfBounds(index) -> throw IndexOutOfBoundsException()
    index >= tailOffset(count) -> tail
    else -> {
      var level = shift
      var node = root

      while (level > 0) {
        node = node.array[(index ushr level) and 0x01f] as Node<E>
        level -= SHIFT
      }

      val value = node.array

      value
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun nth(index: Int): E {
    val leaf = leafArrayBy(index)

    return leaf[index and 0x01f] as E
  }

  override fun asTransient(): TransientVector<E> = TransientVector(this)

  @Suppress("UNCHECKED_CAST")
  private fun popTail(level: Int, node: Node<E>): Node<E>? =
    (((count - 2) ushr level) and 0x01f).let { subIndex ->
      when {
        level > SHIFT -> {
          val newChild = popTail(level - SHIFT, node.array[subIndex] as Node<E>)

          when {
            newChild == null && subIndex == 0 -> null
            else -> {
              val n = Node<E>(node.edit, node.array.copyOf())
              n.array[subIndex] = newChild

              n
            }
          }
        }
        subIndex == 0 -> null
        else -> {
          val n = Node<E>(node.edit, node.array.copyOf())
          n.array[subIndex] = null

          n
        }
      }
    }

  @Suppress("UNCHECKED_CAST")
  override fun pop(): PersistentVector<E> = when {
    isEmpty() -> EmptyVector
    count == 1 -> EmptyVector
    count - tailOffset(count) > 1 -> {
      val newTail = tail.copyOf(count - 1)

      Vector(count - 1, shift, root, newTail)
    }
    else -> {
      val newTail = leafArrayBy(count - 2)

      var newRoot: Node<E>? = popTail(shift, root)
      var newShift = shift

      if (newRoot == null) newRoot = EmptyNode

      if (shift > SHIFT && newRoot.array[1] == null) {
        newRoot = newRoot.array[0] as Node<E>
        newShift -= SHIFT
      }

      Vector(count - 1, newShift, newRoot, newTail)
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun rangedIterator(start: Int, end: Int): Iterator<E> =
    object : Iterator<E> {
      var i = start
      var base = i - (i % BF)
      var array: Array<Any?>? = when {
        start < count -> leafArrayBy(i)
        else -> null
      }

      override fun hasNext(): Boolean = i < end

      override fun next(): E = when {
        hasNext() -> {
          if (i - base == BF) {
            array = leafArrayBy(i)
            base += BF
          }

          array?.get(i++ and 0x01f) as E
        }
        else -> throw NoSuchElementException()
      }
    }

  override fun iterator(): Iterator<E> = rangedIterator(0, count)

  override fun seq(): ISeq<E> = when (count) {
    0 -> Seq.emptySeq()
    else -> ChunkedSeq(this, 0, 0)
  }

  open class Node<out T>(
    val edit: Edit,
    val array: Array<Any?>,
  ) {
    constructor(edit: Edit) : this(edit, arrayOfNulls(BF))

    internal object EmptyNode : Node<Nothing>(Edit(null), arrayOfNulls(BF))
  }

  internal object EmptyVector : PersistentVector<Nothing>(
    0,
    SHIFT,
    EmptyNode,
    arrayOfNulls(0),
  ) {
    override fun toString(): String = "[]"
  }

  internal class Vector<out E>(
    _count: Int,
    _shift: Int,
    _root: Node<E>,
    _tail: Array<Any?>,
  ) : PersistentVector<E>(_count, _shift, _root, _tail)

  internal class ChunkedSeq<out E>(
    val vector: PersistentVector<E>,
    val index: Int,
    val offset: Int,
    val node: Array<Any?> = vector.leafArrayBy(index),
  ) : ASeq<E>(), IChunkedSeq<E>, InstaCount {
    constructor(
      vector: PersistentVector<E>,
      node: Array<Any?>,
      index: Int,
      offset: Int,
    ) : this(vector, index, offset, node)

    @Suppress("UNCHECKED_CAST")
    override fun firstChunk(): Chunk<E> = ArrayChunk(
      node as Array<E>,
      offset,
    )

    override fun restChunks(): ISeq<E> = when {
      index + node.size < vector.size -> ChunkedSeq(
        vector,
        index + node.size,
        0,
      )
      else -> PersistentList.Empty
    }

    override val count: Int
      get() = vector.count - (index + offset)

    @Suppress("UNCHECKED_CAST")
    override fun first(): E = node[offset] as E

    override fun next(): ISeq<E>? = (offset + 1).let {
      when {
        it < node.size -> ChunkedSeq(vector, node, index, it)
        else -> when (val restChunks = restChunks()) {
          is PersistentList.Empty -> null
          else -> restChunks
        }
      }
    }
  }

  class TransientVector<out E> private constructor(
    size: Int,
    shift: Int,
    root: Node<E>,
    tail: Array<Any?>,
  ) : InstaCount, ITransientCollection<E> {

    private val _count: AtomicInt = atomic(size)
    private val _shift: AtomicInt = atomic(shift)
    private val _root: AtomicRef<Node<E>> = atomic(root)
    private val _tail: AtomicRef<Array<Any?>> = atomic(tail)

    fun ensureEditable() {
      if (root.edit.value == null) {
        throw IllegalStateException(
          "Transient used after persistent() call",
        )
      }
    }

    override val count: Int
      get() {
        ensureEditable()
        return _count.value
      }

    val shift: Int by _shift

    val root: Node<E> by _root

    internal var tail: Array<Any?>
      get() = _tail.value
      set(value) {
        _tail.value = value
      }

    internal fun invalidate() {
      root.edit.value = null
    }

    private fun ensureEditable(node: Node<E>): Node<E> = root.edit.let {
      when {
        node.edit === it -> node
        else -> Node(it, node.array.copyOf())
      }
    }

    @Suppress("UNCHECKED_CAST")
    private fun pushTail(level: Int, parent: Node<E>, tail: Node<E>): Node<E> {
      val subIndex = ((count - 1) ushr level) and 0x01f

      val rootNode = ensureEditable(parent)

      val nodeToInsert: Node<E> = if (level == SHIFT) {
        tail
      } else {
        when (val child = rootNode.array[subIndex]) {
          null -> newPath(root.edit, level - 5, tail)
          else -> pushTail(level - SHIFT, child as Node<E>, tail)
        }
      }

      rootNode.array[subIndex] = nodeToInsert

      return rootNode
    }

    private val lock = reentrantLock()

    override fun conj(e: @UnsafeVariance E): TransientVector<E> {
      lock.withLock {
        ensureEditable()

        val oldCount = count
        // empty slot available in tail?
        if (oldCount - tailOffset(oldCount) < BF) {
          tail[oldCount and 0x01f] = e
          _count.incrementAndGet()

          return this
        }

        val tailNode = Node<E>(root.edit, tail)
        tail = arrayOfNulls(BF)
        tail[0] = e

        var newShift = shift
        val newRoot: Node<E>
        if ((count ushr SHIFT) > (1 shl shift)) {
          newRoot = Node(root.edit)
          newRoot.array[0] = root
          newRoot.array[1] = newPath(root.edit, shift, tailNode)
          newShift += SHIFT
        } else newRoot = pushTail(shift, root, tailNode)

        _root.value = newRoot
        _shift.value = newShift
        _count.incrementAndGet()

        return this
      }
    }

    override fun persistent(): PersistentVector<E> {
      ensureEditable()
      invalidate()

      val count = _count.value
      val trimmedTail = arrayOfNulls<Any?>(count - tailOffset(count))

      tail.copyInto(trimmedTail, 0, 0, trimmedTail.size)

      return Vector(count, _shift.value, _root.value, trimmedTail)
    }

    companion object {
      private fun <E> editableRoot(node: Node<E>): Node<E> =
        Node(Edit(Any()), node.array.copyOf())

      private fun maximizeTail(tail: Array<Any?>): Array<Any?> {
        val maxTail = arrayOfNulls<Any?>(BF)

        return tail.copyInto(maxTail, 0, 0, tail.size)
      }

      operator fun <E> invoke(vec: PersistentVector<E>): TransientVector<E> =
        TransientVector(
          vec.count,
          vec.shift,
          editableRoot(vec.root),
          maximizeTail(vec.tail),
        )
    }
  }

  companion object {
    @Suppress("UNCHECKED_CAST")
    internal operator fun <E> invoke(): PersistentVector<E> = EmptyVector

    @Suppress("UNCHECKED_CAST")
    internal operator fun <E> invoke(vararg args: E): PersistentVector<E> =
      when {
        args.isEmpty() -> EmptyVector
        args.size <= BF -> {
          Vector(args.size, SHIFT, EmptyNode, args as Array<Any?>)
        }
        else -> {
          var ret: TransientVector<E> = EmptyVector.asTransient()
          for (item in args) ret = ret.conj(item)
          ret.persistent()
        }
      }

    internal operator fun <E> invoke(seq: ISeq<E>): PersistentVector<E> {
      val tail = arrayOfNulls<Any>(BF)
      var i = 0
      var s: ISeq<E>? = seq
      while (s != null && i < BF) {
        tail[i++] = s.first()
        s = s.next()
      }

      return when {
        s != null -> {
          val start = Vector(BF, SHIFT, EmptyNode, tail)
          var ret: TransientVector<E> = start.asTransient()
          while (s != null) {
            ret = ret.conj(s.first())
            s = s.next()
          }
          ret.persistent()
        }
        i == BF -> Vector(BF, SHIFT, EmptyNode, tail)
        else -> {
          val trimmedTail = arrayOfNulls<Any>(i)
          tail.copyInto(trimmedTail, 0, 0, i)
          Vector(i, 5, EmptyNode, trimmedTail)
        }
      }
    }

    internal fun <E> create(list: List<E>): PersistentVector<E> {
      val size = list.size

      return when {
        size == 0 -> EmptyVector
        size <= BF -> Vector(size, SHIFT, EmptyNode, list.toTypedArray())
        else -> {
          val empty: TransientVector<E> = EmptyVector.asTransient()

          list.fold(empty) { tVec, e -> tVec.conj(e) }.persistent()
        }
      }
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <E> create(list: Iterable<E>): PersistentVector<E> {
      if (list is ArrayList<E>) return create(list)

      val iter: Iterator<*> = list.iterator()

      var ret: TransientVector<E> = EmptyVector.asTransient()
      while (iter.hasNext()) ret = ret.conj(iter.next() as E)

      return ret.persistent()
    }

    private fun tailOffset(count: Int): Int = when {
      count < BF -> 0
      else -> ((count - 1) ushr SHIFT) shl SHIFT
    }

    private tailrec fun <E> newPath(
      edit: Edit,
      level: Int,
      node: Node<E>,
    ): Node<E> {
      if (level == 0) return node

      val path = Node<E>(edit)
      path.array[0] = node

      return newPath(edit, level - SHIFT, path)
    }
  }
}
