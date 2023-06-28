package io.github.yahyatinani.y.core.collections.seq

import io.github.yahyatinani.y.core.collections.APersistentMap
import io.github.yahyatinani.y.core.collections.ASeq
import io.github.yahyatinani.y.core.collections.PersistentList
import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.collections.SeqIterator
import io.github.yahyatinani.y.core.m
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class SeqIteratorTest : FreeSpec({
  "hasNext()" {
    SeqIterator<Int>(null).hasNext().shouldBeFalse()

    SeqIterator<Int>(Empty).hasNext().shouldBeFalse()

    SeqIterator(PersistentList(1)).hasNext().shouldBeTrue()
  }

  "next()" - {
    "when the iterator is empty, it should throw NoSuchElementException" {
      val list: PersistentList<Int> = Empty

      shouldThrowExactly<NoSuchElementException> {
        SeqIterator(list).next()
      }
    }

    """when the iterator is not empty, it should return current item and
           move 1 iteration forward""" {
      val map = m("a" to 1, "b" to 2, "c" to 3)
      val keySeq: ASeq<String> =
        APersistentMap.KeySeq(
          map,
        ) as ASeq<String>
      val iter = keySeq.iterator()

      iter.hasNext().shouldBeTrue()

      iter.next() shouldBe "a"
      iter.next() shouldBe "b"
      iter.next() shouldBe "c"

      iter.hasNext().shouldBeFalse()
    }
  }
})
