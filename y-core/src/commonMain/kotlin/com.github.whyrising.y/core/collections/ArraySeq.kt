package com.github.whyrising.y.core.collections

import com.github.whyrising.y.core.util.equals as equalz

class ArraySeq<out E> internal constructor(
  val array: Array<out E>,
  val i: Int
) : ASeq<E>(), IndexedSeq {

  override fun first(): E = when {
    array.isEmpty() -> throw NoSuchElementException("ArraySeq is empty.")
    else -> array[i]
  }

  override fun next(): ISeq<E>? = when {
    i + 1 < array.size -> ArraySeq(array, i + 1)
    else -> null
  }

  override val count: Int
    get() = array.size - i

  override val index: Int
    get() = i

  override fun indexOf(element: @UnsafeVariance E): Int {
    return when {
      array.isEmpty() -> -1
      else -> {
        for (j in i until array.size)
          if (equalz(array[j], element)) {
            return j - i
          }
        -1
      }
    }
  }

  override fun lastIndexOf(element: @UnsafeVariance E): Int {
    if (array.isNotEmpty()) {
      if (element == null) {
        for (j in array.size - 1 downTo i)
          if (array[j] == null) {
            return j - i
          }
      } else {
        for (j in array.size - 1 downTo i)
          if (equalz(array[j], element)) {
            return j - i
          }
      }
    }
    return -1
  }

  /*
  Primitive versions
   */

  class ShortArraySeq(
    val array: ShortArray,
    val i: Int
  ) : ASeq<Short>(), IndexedSeq {
    override fun first(): Short = when {
      array.isEmpty() -> {
        throw NoSuchElementException("ShortArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Short> = when {
      i + 1 < array.size -> ShortArraySeq(array, i + 1)
      else -> empty() as ISeq<Short>
    }

    override fun next(): ISeq<Short>? = when {
      i + 1 < array.size -> ShortArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Short): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Short): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class IntArraySeq(
    val array: IntArray,
    val i: Int
  ) : ASeq<Int>(), IndexedSeq {
    override fun first(): Int = when {
      array.isEmpty() -> {
        throw NoSuchElementException("IntArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun next(): ISeq<Int>? = when {
      i + 1 < array.size -> IntArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Int): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Int): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class FloatArraySeq(
    val array: FloatArray,
    val i: Int
  ) : ASeq<Float>(), IndexedSeq {
    override fun first(): Float = when {
      array.isEmpty() -> {
        throw NoSuchElementException("FloatArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Float> = when {
      i + 1 < array.size -> FloatArraySeq(array, i + 1)
      else -> empty() as ISeq<Float>
    }

    override fun next(): ISeq<Float>? = when {
      i + 1 < array.size -> FloatArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Float): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Float): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class DoubleArraySeq(
    val array: DoubleArray,
    val i: Int
  ) : ASeq<Double>(), IndexedSeq {
    override fun first(): Double = when {
      array.isEmpty() -> {
        throw NoSuchElementException("DoubleArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Double> = when {
      i + 1 < array.size -> DoubleArraySeq(array, i + 1)
      else -> empty() as ISeq<Double>
    }

    override fun next(): ISeq<Double>? = when {
      i + 1 < array.size -> DoubleArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Double): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Double): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class LongArraySeq(
    val array: LongArray,
    val i: Int
  ) : ASeq<Long>(), IndexedSeq {
    override fun first(): Long = when {
      array.isEmpty() -> {
        throw NoSuchElementException("LongArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Long> = when {
      i + 1 < array.size -> LongArraySeq(array, i + 1)
      else -> empty() as ISeq<Long>
    }

    override fun next(): ISeq<Long>? = when {
      i + 1 < array.size -> LongArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Long): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Long): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class ByteArraySeq(
    val array: ByteArray,
    val i: Int
  ) : ASeq<Byte>(), IndexedSeq {
    override fun first(): Byte = when {
      array.isEmpty() -> {
        throw NoSuchElementException("ByteArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Byte> = when {
      i + 1 < array.size -> ByteArraySeq(array, i + 1)
      else -> empty() as ISeq<Byte>
    }

    override fun next(): ISeq<Byte>? = when {
      i + 1 < array.size -> ByteArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Byte): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Byte): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class CharArraySeq(
    val array: CharArray,
    val i: Int
  ) : ASeq<Char>(), IndexedSeq {
    override fun first(): Char = when {
      array.isEmpty() -> {
        throw NoSuchElementException("CharArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Char> = when {
      i + 1 < array.size -> CharArraySeq(array, i + 1)
      else -> empty() as ISeq<Char>
    }

    override fun next(): ISeq<Char>? = when {
      i + 1 < array.size -> CharArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Char): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Char): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  class BooleanArraySeq(
    val array: BooleanArray,
    val i: Int
  ) : ASeq<Boolean>(), IndexedSeq {
    override fun first(): Boolean = when {
      array.isEmpty() -> {
        throw NoSuchElementException("BooleanArraySeq is empty.")
      }
      else -> array[i]
    }

    override fun rest(): ISeq<Boolean> = when {
      i + 1 < array.size -> BooleanArraySeq(array, i + 1)
      else -> empty() as ISeq<Boolean>
    }

    override fun next(): ISeq<Boolean>? = when {
      i + 1 < array.size -> BooleanArraySeq(array, i + 1)
      else -> null
    }

    override val count: Int
      get() = array.size - i

    override val index: Int
      get() = i

    override fun indexOf(element: Boolean): Int {
      for (j in i until array.size)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }

    override fun lastIndexOf(element: Boolean): Int {
      for (j in array.size - 1 downTo i)
        if (array[j] == element) {
          return j - i
        }

      return -1
    }
  }

  companion object {
    operator fun invoke(array: IntArray): ISeq<Int> {
      return IntArraySeq(array, 0)
    }

    operator fun invoke(array: ShortArray): ISeq<Short> {
      return ShortArraySeq(array, 0)
    }

    operator fun invoke(array: FloatArray): ISeq<Float> {
      return FloatArraySeq(array, 0)
    }

    operator fun invoke(array: DoubleArray): ISeq<Double> {
      return DoubleArraySeq(array, 0)
    }

    operator fun invoke(array: LongArray): ISeq<Long> {
      return LongArraySeq(array, 0)
    }

    operator fun invoke(array: ByteArray): ISeq<Byte> {
      return ByteArraySeq(array, 0)
    }

    operator fun invoke(array: CharArray): ISeq<Char> {
      return CharArraySeq(array, 0)
    }

    operator fun invoke(array: BooleanArray): ISeq<Boolean> {
      return BooleanArraySeq(array, 0)
    }

    operator fun <E> invoke(array: Array<E>): ISeq<E> {
      return ArraySeq(array, 0)
    }
  }
}
