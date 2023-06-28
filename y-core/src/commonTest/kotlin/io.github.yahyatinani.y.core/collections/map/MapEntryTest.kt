package io.github.yahyatinani.y.core.collections.map

import io.github.yahyatinani.y.core.collections.APersistentVector
import io.github.yahyatinani.y.core.collections.IMapEntry
import io.github.yahyatinani.y.core.collections.MapEntry
import io.github.yahyatinani.y.core.collections.PersistentList
import io.github.yahyatinani.y.core.collections.PersistentVector
import io.github.yahyatinani.y.core.collections.PersistentVector.EmptyVector
import io.github.yahyatinani.y.core.l
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class MapEntryTest : FreeSpec({
  "key/value" {
    val entry: IMapEntry<String, Int> = MapEntry("a", 1)

    entry.key shouldBe "a"
    entry.value shouldBeExactly 1
  }

  "nth(index)" {
    val entry = MapEntry("a", 1)

    entry.nth(0) shouldBe "a"
    entry.nth(1) shouldBe 1
    val e = shouldThrowExactly<IndexOutOfBoundsException> {
      entry.nth(2)
    }

    e.message shouldBe "index = 2"
  }

  "count" {
    MapEntry("a", 1).count shouldBeExactly 2
  }

  "empty()" {
    val entry = MapEntry("a", 1)

    entry.empty() shouldBeSameInstanceAs EmptyVector
  }

  "conj(e)" {
    val entry = MapEntry("a", 1)

    val vec = entry.conj(15)

    vec.count shouldBeExactly 3
    vec.nth(0) shouldBe "a"
    vec.nth(1) shouldBe 1
    vec.nth(2) shouldBe 15
  }

  "assocN(index, value)" {
    val entry = MapEntry("a", 1)

    entry.assocN(0, "b").nth(0) shouldBe "b"
    entry.assocN(1, 15).nth(1) shouldBe 15
    entry.assocN(2, 15).nth(2) shouldBe 15
    val e = shouldThrowExactly<IndexOutOfBoundsException> {
      entry.assocN(3, 15)
    }

    e.message shouldBe "index = 3"
  }

  "iterator()" {
    val entry = MapEntry("a", 1)

    val iter = entry.iterator()

    iter.hasNext().shouldBeTrue()

    iter.next() shouldBe "a"
    iter.next() shouldBe 1

    iter.hasNext().shouldBeFalse()
    shouldThrowExactly<NoSuchElementException> { iter.next() }
  }

  @Suppress("UNCHECKED_CAST")
  "pop()" {
    val entry = MapEntry("a", 1)

    val r = entry.pop() as PersistentVector<String>

    r.count shouldBeExactly 1
    r[0] shouldBe "a"
  }

  "seq()" - {
    "it should return a APersistentVector.Seq" {
      val entry = MapEntry("a", 1)

      val s = entry.seq() as APersistentVector.Seq

      s.count shouldBeExactly 2
      s.first() shouldBe "a"
      s.rest() shouldBe l(1)
      s.rest().rest() shouldBe PersistentList.Empty
      s.next() shouldBe l(1)
      s.next()?.next() shouldBe null
    }
  }
})
