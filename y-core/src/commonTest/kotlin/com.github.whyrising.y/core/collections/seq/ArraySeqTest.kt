package com.github.whyrising.y.core.collections.seq

import com.github.whyrising.y.core.collections.ArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.BooleanArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.ByteArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.CharArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.DoubleArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.FloatArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.IntArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.LongArraySeq
import com.github.whyrising.y.core.collections.ArraySeq.ShortArraySeq
import com.github.whyrising.y.core.collections.ISeq
import com.github.whyrising.y.core.collections.PersistentList.Empty
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ArraySeqTest : FreeSpec({
  "ctor()" {
    val array = arrayOf(1, 2, 3)
    val arraySeq: ArraySeq<Int> = ArraySeq(array, 0)

    arraySeq.array shouldBeSameInstanceAs array
    arraySeq.i shouldBeExactly 0
  }

  "first() should return element at i in array" {
    val array = arrayOf(54, 5, 3, 98)
    val i = 0
    val arraySeq: ArraySeq<Int> = ArraySeq(array, i)

    arraySeq.first() shouldBeExactly array[i]
  }

  "first() should throw NoSuchElementException when array is empty" {
    shouldThrowExactly<NoSuchElementException> {
      ArraySeq(arrayOf<Int>(), 0).first()
    }.message shouldBe "ArraySeq is empty."
  }

  "rest() should a new ArraySeq with same array and incremented i" {
    val array = arrayOf(54, 5, 3, 98)
    val i = 0

    val restSeq = ArraySeq(array, i).rest() as ArraySeq<Int>

    restSeq.array shouldBeSameInstanceAs array
    restSeq.i shouldBeExactly i + 1
  }

  "rest() should return Empty seq when array is empty" {
    val restSeq = ArraySeq(arrayOf<Int>(), 0).rest()

    restSeq shouldBeSameInstanceAs Empty
    ArraySeq(arrayOf(1), 0).rest() shouldBeSameInstanceAs Empty
  }

  "next() should a new ArraySeq with same array and incremented i" {
    val array = arrayOf(54, 5, 3, 98)
    val i = 0

    val restSeq = ArraySeq(array, i).next() as ArraySeq<Int>

    restSeq.array shouldBeSameInstanceAs array
    restSeq.i shouldBeExactly i + 1
  }

  "next() should return null when array is empty" {

    ArraySeq<Int>(arrayOf(), 0).next().shouldBeNull()
    ArraySeq(arrayOf(1), 0).next().shouldBeNull()
  }

  "count should return array size minus index" {
    ArraySeq<Int>(arrayOf(), 0).count shouldBeExactly 0

    ArraySeq(arrayOf(1), 0).count shouldBeExactly 1

    ArraySeq(arrayOf(1, 2, 3), 0).count shouldBeExactly 3

    ArraySeq(arrayOf(1), 0).rest().count shouldBeExactly 0

    ArraySeq(arrayOf(54, 5), 0).rest().count shouldBeExactly 1
  }

  "ArraySeq is IndexedSeq" {
    val indexedSeq = ArraySeq(arrayOf(1), 0)
    val indexedSeq1 = ArraySeq(arrayOf(1), 5)
    val indexedSeq2 = ArraySeq(arrayOf(1), 12)

    indexedSeq.index shouldBeExactly indexedSeq.i
    indexedSeq1.index shouldBeExactly indexedSeq1.i
    indexedSeq2.index shouldBeExactly indexedSeq2.i
  }

  "indexOf(element)" {
    ArraySeq<Int>(arrayOf(), 0).indexOf(12) shouldBeExactly -1

    ArraySeq(arrayOf(15, 2, 5), 0).indexOf(52) shouldBeExactly -1

    ArraySeq(arrayOf(15, 2, 5), 0).indexOf(2) shouldBeExactly 1

    (ArraySeq(arrayOf(15, 2, 5), 0).rest() as ArraySeq)
      .indexOf(2) shouldBeExactly 0
  }

  "lastIndexOf(element)" {
    ArraySeq<Int>(arrayOf(), 0).lastIndexOf(12) shouldBeExactly -1

    ArraySeq(arrayOf(5, null, null), 0).lastIndexOf(null) shouldBeExactly 2

    (ArraySeq(arrayOf(5, null, null), 0).rest() as ArraySeq)
      .lastIndexOf(null) shouldBeExactly 1

    ArraySeq(arrayOf(5, 5, 3), 0).lastIndexOf(5) shouldBeExactly 1

    (ArraySeq(arrayOf(5, 5, 5, 3), 0).rest() as ArraySeq)
      .lastIndexOf(5) shouldBeExactly 1

    ArraySeq(arrayOf(5, 5, 3), 0).lastIndexOf(55) shouldBeExactly -1
  }

  "invoke(shortArray)" {
    val array = shortArrayOf(1, 2, 3)
    val arraySeq: ISeq<Short> = ArraySeq(array)

    (arraySeq is ShortArraySeq).shouldBeTrue()
    (arraySeq as ShortArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(intArray)" {
    val array = intArrayOf(1, 2, 3)
    val arraySeq: ISeq<Int> = ArraySeq(array)

    (arraySeq is IntArraySeq).shouldBeTrue()
    (arraySeq as IntArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(floatArray)" {
    val array = floatArrayOf(1F, 2F, 3F)
    val arraySeq: ISeq<Float> = ArraySeq(array)

    (arraySeq is FloatArraySeq).shouldBeTrue()
    (arraySeq as FloatArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(doubleArray)" {
    val array = doubleArrayOf(1.0, 2.0, 3.0)
    val arraySeq: ISeq<Double> = ArraySeq(array)

    (arraySeq is DoubleArraySeq).shouldBeTrue()
    (arraySeq as DoubleArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(longArray)" {
    val array = longArrayOf(1, 2, 3)
    val arraySeq: ISeq<Long> = ArraySeq(array)

    (arraySeq is LongArraySeq).shouldBeTrue()
    (arraySeq as LongArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(byteArray)" {
    val array = byteArrayOf(1, 2, 3)
    val arraySeq: ISeq<Byte> = ArraySeq(array)

    (arraySeq is ByteArraySeq).shouldBeTrue()
    (arraySeq as ByteArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(charArrayOf)" {
    val array = charArrayOf('1', '2', '3')
    val arraySeq: ISeq<Char> = ArraySeq(array)

    (arraySeq is CharArraySeq).shouldBeTrue()
    (arraySeq as CharArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(booleanArray)" {
    val array = booleanArrayOf(true, false, true)
    val arraySeq: ISeq<Boolean> = ArraySeq(array)

    (arraySeq is BooleanArraySeq).shouldBeTrue()
    (arraySeq as BooleanArraySeq).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "invoke(array)" {
    val array = arrayOf("a", 1, 5.0)
    val arraySeq: ISeq<Any> = ArraySeq(array)

    (arraySeq is ArraySeq<*>).shouldBeTrue()
    (arraySeq as ArraySeq<*>).i shouldBeExactly 0
    arraySeq.array shouldBeSameInstanceAs array
  }

  "ShortArraySeqTest" - {
    "ctor" {
      val array = shortArrayOf(1, 2, 3)
      val arraySeq = ShortArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = shortArrayOf(54, 5, 3, 98)
      val i = 0
      val arraySeq = ShortArraySeq(array, i)

      arraySeq.first() shouldBe array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        ShortArraySeq(shortArrayOf(), 0).first()
      }.message shouldBe "ShortArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = shortArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = ShortArraySeq(array, i).rest() as ShortArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = ShortArraySeq(shortArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty
      ShortArraySeq(
        shortArrayOf(1),
        0
      ).rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = shortArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = ShortArraySeq(array, i).rest() as ShortArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      ShortArraySeq(shortArrayOf(), 0).next().shouldBeNull()
      ShortArraySeq(shortArrayOf(1), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      ShortArraySeq(shortArrayOf(), 0).count shouldBeExactly 0

      ShortArraySeq(shortArrayOf(1), 0).count shouldBeExactly 1

      ShortArraySeq(shortArrayOf(1, 2, 3), 0).count shouldBeExactly 3

      ShortArraySeq(shortArrayOf(1), 0).rest().count shouldBeExactly 0

      ShortArraySeq(shortArrayOf(54, 5), 0).rest().count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = ShortArraySeq(shortArrayOf(1), 0)
      val indexedSeq1 = ShortArraySeq(shortArrayOf(1), 5)
      val indexedSeq2 = ShortArraySeq(shortArrayOf(1), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      ShortArraySeq(shortArrayOf(), 0).indexOf(12) shouldBeExactly -1

      ShortArraySeq(
        shortArrayOf(15, 2, 5),
        0
      ).indexOf(52) shouldBeExactly -1

      ShortArraySeq(
        shortArrayOf(15, 2, 5),
        0
      ).indexOf(2) shouldBeExactly 1

      (ShortArraySeq(shortArrayOf(15, 2, 5), 0).rest() as ShortArraySeq)
        .indexOf(2) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      ShortArraySeq(shortArrayOf(), 0).lastIndexOf(12) shouldBeExactly -1

      ShortArraySeq(
        shortArrayOf(5, 5, 3),
        0
      ).lastIndexOf(3) shouldBeExactly 2

      (ShortArraySeq(shortArrayOf(5, 3, 3), 0).rest() as ShortArraySeq)
        .lastIndexOf(3) shouldBeExactly 1

      ShortArraySeq(
        shortArrayOf(5, 5, 3),
        0
      ).lastIndexOf(5) shouldBeExactly 1

      (ShortArraySeq(shortArrayOf(5, 5, 5, 3), 0).rest() as ShortArraySeq)
        .lastIndexOf(5) shouldBeExactly 1

      ShortArraySeq(shortArrayOf(5, 5, 3), 0)
        .lastIndexOf(55) shouldBeExactly -1
    }
  }

  "IntArraySeqTest" - {
    "ctor" {
      val array = intArrayOf(1, 2, 3)
      val arraySeq = IntArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = intArrayOf(54, 5, 3, 98)
      val i = 0
      val arraySeq = IntArraySeq(array, i)

      arraySeq.first() shouldBeExactly array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        IntArraySeq(intArrayOf(), 0).first()
      }.message shouldBe "IntArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = intArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = IntArraySeq(array, i).rest() as IntArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = IntArraySeq(intArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty
      IntArraySeq(intArrayOf(1), 0).rest() shouldBeSameInstanceAs Empty
    }

    "count should return array size minus index" {
      IntArraySeq(intArrayOf(), 0).count shouldBeExactly 0

      IntArraySeq(intArrayOf(1), 0).count shouldBeExactly 1

      IntArraySeq(intArrayOf(1, 2, 3), 0).count shouldBeExactly 3

      IntArraySeq(intArrayOf(1), 0).rest().count shouldBeExactly 0

      IntArraySeq(intArrayOf(54, 5), 0).rest().count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = IntArraySeq(intArrayOf(1), 0)
      val indexedSeq1 = IntArraySeq(intArrayOf(1), 5)
      val indexedSeq2 = IntArraySeq(intArrayOf(1), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      IntArraySeq(intArrayOf(), 0).indexOf(12) shouldBeExactly -1

      IntArraySeq(intArrayOf(15, 2, 5), 0).indexOf(52) shouldBeExactly -1

      IntArraySeq(intArrayOf(15, 2, 5), 0).indexOf(2) shouldBeExactly 1

      (IntArraySeq(intArrayOf(15, 2, 5), 0).rest() as IntArraySeq)
        .indexOf(2) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      IntArraySeq(intArrayOf(), 0).lastIndexOf(12) shouldBeExactly -1

      IntArraySeq(intArrayOf(5, 5, 3), 0).lastIndexOf(3) shouldBeExactly 2

      (IntArraySeq(intArrayOf(5, 3, 3), 0).rest() as IntArraySeq)
        .lastIndexOf(3) shouldBeExactly 1

      IntArraySeq(intArrayOf(5, 5, 3), 0).lastIndexOf(5) shouldBeExactly 1

      (IntArraySeq(intArrayOf(5, 5, 5, 3), 0).rest() as IntArraySeq)
        .lastIndexOf(5) shouldBeExactly 1

      IntArraySeq(
        intArrayOf(5, 5, 3),
        0
      ).lastIndexOf(55) shouldBeExactly -1
    }
  }

  "FloatArraySeqTest" - {
    "ctor" {
      val array = floatArrayOf(1F, 2F, 3F)
      val arraySeq = FloatArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = floatArrayOf(54.5F, 5F, 3F, 98F)
      val i = 0
      val arraySeq = FloatArraySeq(array, i)

      arraySeq.first() shouldBeExactly array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        FloatArraySeq(floatArrayOf(), 0).first()
      }.message shouldBe "FloatArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = floatArrayOf(54F, 5F, 3F, 98F)
      val i = 0

      val restSeq = FloatArraySeq(array, i).rest() as FloatArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = FloatArraySeq(floatArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty
      FloatArraySeq(
        floatArrayOf(1F),
        0
      ).rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = floatArrayOf(54F, 5F, 3F, 98F)
      val i = 0

      val restSeq = FloatArraySeq(array, i).next() as FloatArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      FloatArraySeq(floatArrayOf(), 0).next().shouldBeNull()
      FloatArraySeq(floatArrayOf(1F), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      FloatArraySeq(floatArrayOf(), 0).count shouldBeExactly 0

      FloatArraySeq(floatArrayOf(1F), 0).count shouldBeExactly 1

      FloatArraySeq(floatArrayOf(1F, 2F, 3F), 0).count shouldBeExactly 3

      FloatArraySeq(floatArrayOf(1F), 0).rest().count shouldBeExactly 0

      FloatArraySeq(
        floatArrayOf(54F, 5F),
        0
      ).rest().count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = FloatArraySeq(floatArrayOf(1F), 0)
      val indexedSeq1 = FloatArraySeq(floatArrayOf(1F), 5)
      val indexedSeq2 = FloatArraySeq(floatArrayOf(1F), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      FloatArraySeq(floatArrayOf(), 0).indexOf(12F) shouldBeExactly -1

      FloatArraySeq(floatArrayOf(15F, 2F, 5F), 0)
        .indexOf(52F) shouldBeExactly -1

      FloatArraySeq(floatArrayOf(15F, 2F, 5F), 0)
        .indexOf(2F) shouldBeExactly 1

      (FloatArraySeq(
        floatArrayOf(15F, 2F, 5F),
        0
      ).rest() as FloatArraySeq)
        .indexOf(2F) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      FloatArraySeq(floatArrayOf(), 0).lastIndexOf(12F) shouldBeExactly -1

      FloatArraySeq(floatArrayOf(5F, 5F, 3F), 0)
        .lastIndexOf(3F) shouldBeExactly 2

      (FloatArraySeq(floatArrayOf(5F, 3F, 3F), 0).rest() as FloatArraySeq)
        .lastIndexOf(3F) shouldBeExactly 1

      FloatArraySeq(floatArrayOf(5F, 5F, 3F), 0)
        .lastIndexOf(5F) shouldBeExactly 1

      (FloatArraySeq(
        floatArrayOf(5F, 5F, 5F, 3F),
        0
      ).rest() as FloatArraySeq)
        .lastIndexOf(5F) shouldBeExactly 1

      FloatArraySeq(floatArrayOf(5F, 5F, 3F), 0)
        .lastIndexOf(55F) shouldBeExactly -1
    }
  }

  "DoubleArraySeqTest" - {
    "ctor" {
      val array = doubleArrayOf(1.0, 2.0, 3.0)
      val arraySeq = DoubleArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = doubleArrayOf(54.5, 5.0, 3.0, 98.0)
      val i = 0
      val arraySeq = DoubleArraySeq(array, i)

      arraySeq.first() shouldBeExactly array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        DoubleArraySeq(doubleArrayOf(), 0).first()
      }.message shouldBe "DoubleArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = doubleArrayOf(54.0, 5.0, 3.0, 98.0)
      val i = 0

      val restSeq = DoubleArraySeq(array, i).rest() as DoubleArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = DoubleArraySeq(doubleArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty

      DoubleArraySeq(doubleArrayOf(1.0), 0)
        .rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = doubleArrayOf(54.0, 5.0, 3.0, 98.0)
      val i = 0

      val restSeq = DoubleArraySeq(array, i).rest() as DoubleArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      DoubleArraySeq(doubleArrayOf(), 0).next().shouldBeNull()
      DoubleArraySeq(doubleArrayOf(1.0), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      DoubleArraySeq(doubleArrayOf(), 0).count shouldBeExactly 0

      DoubleArraySeq(doubleArrayOf(1.0), 0).count shouldBeExactly 1

      DoubleArraySeq(
        doubleArrayOf(1.0, 2.0, 3.0),
        0
      ).count shouldBeExactly 3

      DoubleArraySeq(doubleArrayOf(1.0), 0).rest().count shouldBeExactly 0

      DoubleArraySeq(doubleArrayOf(54.0, 5.0), 0).rest()
        .count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = DoubleArraySeq(doubleArrayOf(1.0), 0)
      val indexedSeq1 = DoubleArraySeq(doubleArrayOf(1.0), 5)
      val indexedSeq2 = DoubleArraySeq(doubleArrayOf(1.0), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      DoubleArraySeq(doubleArrayOf(), 0).indexOf(12.0) shouldBeExactly -1

      DoubleArraySeq(doubleArrayOf(15.0, 2.0, 5.0), 0)
        .indexOf(52.0) shouldBeExactly -1

      DoubleArraySeq(doubleArrayOf(15.0, 2.0, 5.0), 0)
        .indexOf(2.0) shouldBeExactly 1

      val doubleArraySeq = DoubleArraySeq(
        doubleArrayOf(15.0, 2.0, 5.0),
        0
      ).rest() as DoubleArraySeq
      doubleArraySeq.indexOf(2.0) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      DoubleArraySeq(
        doubleArrayOf(),
        0
      ).lastIndexOf(12.0) shouldBeExactly -1

      DoubleArraySeq(doubleArrayOf(5.0, 5.0, 3.0), 0)
        .lastIndexOf(3.0) shouldBeExactly 2

      val doubleArraySeq = DoubleArraySeq(doubleArrayOf(5.0, 3.0, 3.0), 0)
        .rest() as DoubleArraySeq
      doubleArraySeq.lastIndexOf(3.0) shouldBeExactly 1

      DoubleArraySeq(doubleArrayOf(5.0, 5.0, 3.0), 0)
        .lastIndexOf(5.0) shouldBeExactly 1

      val doubleArraySeq1 =
        DoubleArraySeq(doubleArrayOf(5.0, 5.0, 5.0, 3.0), 0)
          .rest() as DoubleArraySeq
      doubleArraySeq1.lastIndexOf(5.0) shouldBeExactly 1

      DoubleArraySeq(doubleArrayOf(5.0, 5.0, 3.0), 0)
        .lastIndexOf(55.0) shouldBeExactly -1
    }
  }

  "LongArraySeqTest" - {
    "ctor" {
      val array = longArrayOf(1, 2, 3)
      val arraySeq = LongArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = longArrayOf(54, 5, 3, 98)
      val i = 0
      val arraySeq = LongArraySeq(array, i)

      arraySeq.first() shouldBeExactly array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        LongArraySeq(longArrayOf(), 0).first()
      }.message shouldBe "LongArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = longArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = LongArraySeq(array, i).rest() as LongArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = LongArraySeq(longArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty
      LongArraySeq(longArrayOf(1), 0).rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = longArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = LongArraySeq(array, i).next() as LongArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      LongArraySeq(longArrayOf(), 0).next().shouldBeNull()
      LongArraySeq(longArrayOf(1), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      LongArraySeq(longArrayOf(), 0).count shouldBeExactly 0

      LongArraySeq(longArrayOf(1), 0).count shouldBeExactly 1

      LongArraySeq(longArrayOf(1, 2, 3), 0).count shouldBeExactly 3

      LongArraySeq(longArrayOf(1), 0).rest().count shouldBeExactly 0

      LongArraySeq(longArrayOf(54, 5), 0).rest().count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = LongArraySeq(longArrayOf(1), 0)
      val indexedSeq1 = LongArraySeq(longArrayOf(1), 5)
      val indexedSeq2 = LongArraySeq(longArrayOf(1), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      LongArraySeq(longArrayOf(), 0).indexOf(12) shouldBeExactly -1

      LongArraySeq(
        longArrayOf(15, 2, 5),
        0
      ).indexOf(52) shouldBeExactly -1

      LongArraySeq(longArrayOf(15, 2, 5), 0).indexOf(2) shouldBeExactly 1

      (LongArraySeq(longArrayOf(15, 2, 5), 0).rest() as LongArraySeq)
        .indexOf(2) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      LongArraySeq(longArrayOf(), 0).lastIndexOf(12) shouldBeExactly -1

      LongArraySeq(
        longArrayOf(5, 5, 3),
        0
      ).lastIndexOf(3) shouldBeExactly 2

      (LongArraySeq(longArrayOf(5, 3, 3), 0).rest() as LongArraySeq)
        .lastIndexOf(3) shouldBeExactly 1

      LongArraySeq(
        longArrayOf(5, 5, 3),
        0
      ).lastIndexOf(5) shouldBeExactly 1

      (LongArraySeq(longArrayOf(5, 5, 5, 3), 0).rest() as LongArraySeq)
        .lastIndexOf(5) shouldBeExactly 1

      LongArraySeq(
        longArrayOf(5, 5, 3),
        0
      ).lastIndexOf(55) shouldBeExactly -1
    }
  }

  "ByteArraySeqTest" - {
    "ctor" {
      val array = byteArrayOf(1, 2, 3)
      val arraySeq = ByteArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = byteArrayOf(54, 5, 3, 98)
      val i = 0
      val arraySeq = ByteArraySeq(array, i)

      arraySeq.first() shouldBe array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        ByteArraySeq(byteArrayOf(), 0).first()
      }.message shouldBe "ByteArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = byteArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = ByteArraySeq(array, i).rest() as ByteArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = ByteArraySeq(byteArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty
      ByteArraySeq(byteArrayOf(1), 0).rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = byteArrayOf(54, 5, 3, 98)
      val i = 0

      val restSeq = ByteArraySeq(array, i).next() as ByteArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      ByteArraySeq(byteArrayOf(), 0).next().shouldBeNull()
      ByteArraySeq(byteArrayOf(1), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      ByteArraySeq(byteArrayOf(), 0).count shouldBeExactly 0

      ByteArraySeq(byteArrayOf(1), 0).count shouldBeExactly 1

      ByteArraySeq(byteArrayOf(1, 2, 3), 0).count shouldBeExactly 3

      ByteArraySeq(byteArrayOf(1), 0).rest().count shouldBeExactly 0

      ByteArraySeq(byteArrayOf(54, 5), 0).rest().count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = ByteArraySeq(byteArrayOf(1), 0)
      val indexedSeq1 = ByteArraySeq(byteArrayOf(1), 5)
      val indexedSeq2 = ByteArraySeq(byteArrayOf(1), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      ByteArraySeq(byteArrayOf(), 0).indexOf(12) shouldBeExactly -1

      ByteArraySeq(
        byteArrayOf(15, 2, 5),
        0
      ).indexOf(52) shouldBeExactly -1

      ByteArraySeq(byteArrayOf(15, 2, 5), 0).indexOf(2) shouldBeExactly 1

      (ByteArraySeq(byteArrayOf(15, 2, 5), 0).rest() as ByteArraySeq)
        .indexOf(2) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      ByteArraySeq(byteArrayOf(), 0).lastIndexOf(12) shouldBeExactly -1

      ByteArraySeq(
        byteArrayOf(5, 5, 3),
        0
      ).lastIndexOf(3) shouldBeExactly 2

      (ByteArraySeq(byteArrayOf(5, 3, 3), 0).rest() as ByteArraySeq)
        .lastIndexOf(3) shouldBeExactly 1

      ByteArraySeq(
        byteArrayOf(5, 5, 3),
        0
      ).lastIndexOf(5) shouldBeExactly 1

      (ByteArraySeq(byteArrayOf(5, 5, 5, 3), 0).rest() as ByteArraySeq)
        .lastIndexOf(5) shouldBeExactly 1

      ByteArraySeq(
        byteArrayOf(5, 5, 3),
        0
      ).lastIndexOf(55) shouldBeExactly -1
    }
  }

  "CharArraySeqTest" - {
    "ctor" {
      val array = charArrayOf('1', '2', '3')
      val arraySeq = CharArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = charArrayOf('4', '5', '3', '8')
      val i = 0
      val arraySeq = CharArraySeq(array, i)

      arraySeq.first() shouldBe array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        CharArraySeq(charArrayOf(), 0).first()
      }.message shouldBe "CharArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = charArrayOf('4', '5', '3', '8')
      val i = 0

      val restSeq = CharArraySeq(array, i).rest() as CharArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      val restSeq = CharArraySeq(charArrayOf(), 0).rest()

      restSeq shouldBeSameInstanceAs Empty
      CharArraySeq(
        charArrayOf('1'),
        0
      ).rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = charArrayOf('4', '5', '3', '8')
      val i = 0

      val restSeq = CharArraySeq(array, i).next() as CharArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      CharArraySeq(charArrayOf(), 0).next().shouldBeNull()
      CharArraySeq(charArrayOf('1'), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      CharArraySeq(charArrayOf(), 0).count shouldBeExactly 0

      CharArraySeq(charArrayOf('1'), 0).count shouldBeExactly 1

      CharArraySeq(charArrayOf('1', '2', '3'), 0).count shouldBeExactly 3

      CharArraySeq(charArrayOf('1'), 0).rest().count shouldBeExactly 0

      CharArraySeq(
        charArrayOf('4', '5'),
        0
      ).rest().count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = CharArraySeq(charArrayOf('1'), 0)
      val indexedSeq1 = CharArraySeq(charArrayOf('1'), 5)
      val indexedSeq2 = CharArraySeq(charArrayOf('1'), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      CharArraySeq(charArrayOf(), 0).indexOf('2') shouldBeExactly -1

      CharArraySeq(charArrayOf('1', '2', '5'), 0)
        .indexOf('0') shouldBeExactly -1

      CharArraySeq(charArrayOf('5', '2', '5'), 0)
        .indexOf('2') shouldBeExactly 1

      (CharArraySeq(charArrayOf('5', '2', '5'), 0).rest() as CharArraySeq)
        .indexOf('2') shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      CharArraySeq(charArrayOf(), 0).lastIndexOf('0') shouldBeExactly -1

      CharArraySeq(charArrayOf('5', '5', '3'), 0)
        .lastIndexOf('3') shouldBeExactly 2

      (CharArraySeq(charArrayOf('5', '3', '3'), 0).rest() as CharArraySeq)
        .lastIndexOf('3') shouldBeExactly 1

      CharArraySeq(charArrayOf('5', '5', '3'), 0)
        .lastIndexOf('5') shouldBeExactly 1

      val charArraySeq = CharArraySeq(charArrayOf('5', '5', '5', '3'), 0)
        .rest() as CharArraySeq
      charArraySeq.lastIndexOf('5') shouldBeExactly 1

      CharArraySeq(charArrayOf('5', '5', '3'), 0)
        .lastIndexOf('9') shouldBeExactly -1
    }
  }

  "BooleanArraySeqTest" - {
    "ctor" {
      val array = booleanArrayOf(true, false)
      val arraySeq = BooleanArraySeq(array, 0)

      arraySeq.array shouldBeSameInstanceAs array
      arraySeq.i shouldBeExactly 0
    }

    "first() should return element at i in array" {
      val array = booleanArrayOf(true, false)
      val i = 0
      val arraySeq = BooleanArraySeq(array, i)

      arraySeq.first() shouldBe array[i]
    }

    "first() should throw NoSuchElementException when array is empty" {
      shouldThrowExactly<NoSuchElementException> {
        BooleanArraySeq(booleanArrayOf(), 0).first()
      }.message shouldBe "BooleanArraySeq is empty."
    }

    "rest() should a new ArraySeq with same array and incremented i" {
      val array = booleanArrayOf(true, false, true, false)
      val i = 0

      val restSeq = BooleanArraySeq(array, i).rest() as BooleanArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "rest() should return Empty seq when array is empty" {
      BooleanArraySeq(
        booleanArrayOf(),
        0
      ).rest() shouldBeSameInstanceAs Empty

      BooleanArraySeq(booleanArrayOf(true), 0)
        .rest() shouldBeSameInstanceAs Empty
    }

    "next() should a new ArraySeq with same array and incremented i" {
      val array = booleanArrayOf(true, false, true, false)
      val i = 0

      val restSeq = BooleanArraySeq(array, i).next() as BooleanArraySeq

      restSeq.array shouldBeSameInstanceAs array
      restSeq.i shouldBeExactly i + 1
    }

    "next() should return null when array is empty" {
      BooleanArraySeq(booleanArrayOf(), 0).next().shouldBeNull()
      BooleanArraySeq(booleanArrayOf(true), 0).next().shouldBeNull()
    }

    "count should return array size minus index" {
      BooleanArraySeq(booleanArrayOf(), 0).count shouldBeExactly 0

      BooleanArraySeq(booleanArrayOf(true), 0).count shouldBeExactly 1

      BooleanArraySeq(booleanArrayOf(true, true, true), 0)
        .count shouldBeExactly 3

      BooleanArraySeq(
        booleanArrayOf(true),
        0
      ).rest().count shouldBeExactly 0

      BooleanArraySeq(booleanArrayOf(true, true), 0).rest()
        .count shouldBeExactly 1
    }

    "IntArraySeq is IndexedSeq" {
      val indexedSeq = BooleanArraySeq(booleanArrayOf(true), 0)
      val indexedSeq1 = BooleanArraySeq(booleanArrayOf(true), 5)
      val indexedSeq2 = BooleanArraySeq(booleanArrayOf(true), 12)

      indexedSeq.index shouldBeExactly indexedSeq.i
      indexedSeq1.index shouldBeExactly indexedSeq1.i
      indexedSeq2.index shouldBeExactly indexedSeq2.i
    }

    "indexOf(element)" {
      BooleanArraySeq(
        booleanArrayOf(),
        0
      ).indexOf(true) shouldBeExactly -1

      BooleanArraySeq(booleanArrayOf(true, true), 0)
        .indexOf(false) shouldBeExactly -1

      BooleanArraySeq(booleanArrayOf(true, false, true), 0)
        .indexOf(false) shouldBeExactly 1

      val booleanArraySeq =
        BooleanArraySeq(booleanArrayOf(true, false, true), 0)
          .rest() as BooleanArraySeq
      booleanArraySeq.indexOf(false) shouldBeExactly 0
    }

    "lastIndexOf(element)" {
      BooleanArraySeq(booleanArrayOf(), 0)
        .lastIndexOf(true) shouldBeExactly -1

      BooleanArraySeq(booleanArrayOf(true, true, false), 0)
        .lastIndexOf(false) shouldBeExactly 2

      val booleanArraySeq =
        BooleanArraySeq(booleanArrayOf(true, false, false), 0)
          .rest() as BooleanArraySeq
      booleanArraySeq.lastIndexOf(false) shouldBeExactly 1

      BooleanArraySeq(booleanArrayOf(true, true, false), 0)
        .lastIndexOf(true) shouldBeExactly 1

      val booleanArraySeq1 =
        BooleanArraySeq(booleanArrayOf(true, true, true, false), 0)
          .rest() as BooleanArraySeq
      booleanArraySeq1.lastIndexOf(true) shouldBeExactly 1

      BooleanArraySeq(booleanArrayOf(true, true), 0)
        .lastIndexOf(false) shouldBeExactly -1
    }
  }
})
