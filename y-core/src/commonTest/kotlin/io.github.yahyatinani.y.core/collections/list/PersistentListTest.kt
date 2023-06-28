package io.github.yahyatinani.y.core.collections.list

import io.github.yahyatinani.y.core.collections.ASeq
import io.github.yahyatinani.y.core.collections.IPersistentList
import io.github.yahyatinani.y.core.collections.ISeq
import io.github.yahyatinani.y.core.collections.PersistentList
import io.github.yahyatinani.y.core.collections.PersistentList.Cons
import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.collections.SeqIterator
import io.github.yahyatinani.y.core.l
import io.github.yahyatinani.y.core.mocks.MockSeq
import io.github.yahyatinani.y.core.mocks.User
import io.github.yahyatinani.y.core.toPlist
import io.github.yahyatinani.y.core.util.HASH_PRIME
import io.github.yahyatinani.y.core.util.Murmur3
import io.github.yahyatinani.y.core.v
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.checkAll

class PersistentListTest : FreeSpec({
  "PersistentList" - {
    "invoke() should return Empty list" {
      val emptyList = PersistentList<Int>()

      emptyList shouldBe Empty
    }

    "invoke(vararg) should return a PersistentList.Cons list" {
      val list = PersistentList(1, 2, 3, 4)
      list.count shouldBeExactly 4

      list.first() shouldBeExactly 1
    }

    "conj() should return a persistent list of 1 element" {
      val list = PersistentList(1, 2, 3, 4)

      val r = list.conj(5)

      r.count shouldBeExactly list.size + 1
      r.first() shouldBeExactly 5
    }

    "cons(x)" - {
      "cons(x) should return a seq of 1 element x" {
        checkAll { i: Int ->
          val seq: ISeq<Int> = (Empty as ISeq<Int>).cons(i)

          seq.first() shouldBe i
          (seq as Cons<Int>).rest shouldBe Empty
          seq.count shouldBe 1
        }
      }

      "cons(x) should add x as the first element to this seq" {
        val oldList = Cons(10, Empty)

        checkAll { i: Int ->

          val newList = oldList.cons(i)

          (newList as IPersistentList<*>).count shouldBeExactly 2
          newList.first() shouldBeExactly i
          newList.rest() shouldBeSameInstanceAs oldList
        }
      }
    }

    "equals(x)" {
      val l = l(1, 2, 3)

      l.equals(null).shouldBeFalse()

      (l == l).shouldBeTrue()

      (l == mapOf(1 to 2)).shouldBeFalse()

      (l == MockSeq(v(1, 2, 3))).shouldBeTrue()

      (l == listOf(1, 2, 3, 4)).shouldBeFalse()

      (l<Int>() == listOf<Int>()).shouldBeTrue()

      l.equals(null).shouldBeFalse()

      (l == mapOf(1 to 2)).shouldBeFalse()

      (l == listOf(1, 2, 3)).shouldBeTrue()

      (l == listOf(1, 2, 3, 4)).shouldBeFalse()

      (l(User(1)) == listOf(User(2))).shouldBeFalse()

      (l == l(1, 2, 8)).shouldBeFalse()

      (l == Empty).shouldBeFalse()

      (l == MockSeq(v(1, 2, 3))).shouldBeTrue()

      (l == MockSeq(v(1, 2, 4))).shouldBeFalse()

      (l == MockSeq(v(1, 2, 3, 4))).shouldBeFalse()

      (l == l).shouldBeTrue()
    }

    "equiv(x)" {
      // assert equals behaviour
      l<Int>().equiv(listOf<Int>()).shouldBeTrue()

      l(1, 2, 3).equiv(null).shouldBeFalse()

      l(1, 2, 3).equiv(mapOf(1 to 2)).shouldBeFalse()

      l(1, 2, 3).equiv(listOf(1, 2, 3, 4)).shouldBeFalse()

      l(User(1)).equiv(listOf(User(2))).shouldBeFalse()

      l(1, 2, 3).equiv(l(1, 2, 8)).shouldBeFalse()

      l(1, 2, 3).equiv(Empty).shouldBeFalse()

      l(1, 2, 3).equiv(MockSeq(v(1, 2, 4))).shouldBeFalse()

      l(1, 2, 3).equiv(MockSeq(v(1, 2, 3, 4))).shouldBeFalse()

      l(1).equiv(l(1)).shouldBeTrue()

      l(1, 2, 3).equiv(listOf(1, 2, 3)).shouldBeTrue()

      l(1).equiv(arrayListOf(1)).shouldBeTrue()

      l(1, 2, 3).equiv(MockSeq(v(1, 2, 3))).shouldBeTrue()

      // assert equiv behaviour
      l(1).equiv("list").shouldBeFalse()

      l(1).equiv(l(2, null)).shouldBeFalse()

      l(2, null).equiv(l(2, 3)).shouldBeFalse()

      l(null, 2).equiv(l(2, 3)).shouldBeFalse()

      l(Any()).equiv(setOf(Any())).shouldBeFalse()

      l(1).equiv(l(1L)).shouldBeTrue()

      l(l(1)).equiv(l(listOf(1L))).shouldBeTrue()

      l(l(1L)).equiv(PersistentList(listOf(1))).shouldBeTrue()

      l(1.1).equiv(v(1.1)).shouldBeTrue()
    }

    "peek()" {
      l<Int>().peek().shouldBeNull()
      l(5).peek() shouldBe 5
      l(1, 2, 3).peek() shouldBe 1
      l(5, 4, 8).peek() shouldBe 5
    }

    "pop()" {
      l<Int>().pop() shouldBeSameInstanceAs Empty
      l(1).pop() shouldBeSameInstanceAs Empty
      l(1, 2, 3).pop() shouldBe l(2, 3)
    }

    "+ operator should act like conj" {
      val list = l(1, 2)

      val r = list + 5

      r.count shouldBeExactly list.count + 1
      (r as ISeq<Int>).first() shouldBeExactly 5
    }

    "hashCode()" {
      val integers = (1..20).toList()
      val expectedHash =
        integers.fold(Empty.hashCode()) { hashCode: Int, i: Int? ->
          HASH_PRIME * hashCode + i.hashCode()
        }
      val l = PersistentList(*integers.toTypedArray())

      l.hashCode() shouldBeExactly expectedHash
      l.hashCode shouldBeExactly expectedHash

      Empty.hashCode() shouldBeExactly 1
      Empty.hashCode shouldBeExactly 1
    }

    "hasheq()" {
      val list = l(1, 2, 3, 4, 5) as ASeq<Int>
      val expectedHash = Murmur3.hashOrdered(list)

      list.hasheq() shouldBeExactly expectedHash
      list.hasheq shouldBeExactly expectedHash
      l<Int>().hasheq() shouldBeExactly -2017569654
    }
  }

  "PersistentList.Empty" - {

    "implementation of List_E" - {
      "size should return 0" {
        Empty.size shouldBeExactly 0
      }

      "contains(element) should return false" {
        Empty.contains<Int?>(1).shouldBeFalse()
      }

      "containsAll(coll) should return false when coll is not empty" {
        Empty.containsAll<Int?>(listOf(1)).shouldBeFalse()
      }

      "containsAll(coll) should return true when coll is empty" {
        Empty.containsAll<Int?>(listOf<Int>()).shouldBeTrue()
      }

      "get(index) should throw IndexOutOfBoundsException" {
        val e = shouldThrow<IndexOutOfBoundsException> {
          Empty[0]
        }

        e.message shouldBe "Can't call get on an empty list"
      }

      "indexOf(element) should return -1" {
        val empty: List<Int?> = Empty

        empty.indexOf(10) shouldBeExactly -1
      }

      "isEmpty() should return false" {
        Empty.isEmpty().shouldBeTrue()
      }

      "iterator() should return an iterator" {
        val iterator: Iterator<Int> = Empty.iterator()

        iterator.hasNext().shouldBeFalse()
        shouldThrowExactly<NoSuchElementException> {
          iterator.next()
        }
      }

      "lastIndexOf should return -1" {
        val empty: List<Int?> = Empty

        empty.lastIndexOf(1) shouldBeExactly -1
      }

      "listIterator() should return a ListIterator" {
        val expIter = listOf<Int>().listIterator()
        val empty: List<Int?> = Empty

        empty.listIterator() shouldBe expIter
      }

      "listIterator(Int) should throw IndexOutOfBoundsException" {
        val empty: List<Int?> = Empty

        shouldThrowExactly<IndexOutOfBoundsException> {
          empty.listIterator(1)
        }
      }

      "subList(from, to) should throw IndexOutOfBoundsException" {
        val empty: List<Int?> = Empty

        shouldThrowExactly<IndexOutOfBoundsException> {
          empty.subList(0, 1)
        }
      }
    }

    "toString() should return ()" {
      Empty.toString() shouldBe "()"
    }

    "count property should return return 0" {
      Empty.count shouldBe 0
    }

    "empty() function should return true" {
      Empty.empty() shouldBe Empty
    }

    "equiv()" {
      Empty.equiv(Empty) shouldBe true
    }

    "first property should throw NoSuchElementException" {

      val e = shouldThrowExactly<NoSuchElementException> { Empty.first() }

      e.message shouldBe "Calling first() on empty PersistentList."
    }

    "rest property should return Empty" {
      val rest: ISeq<Int> = Empty.rest()

      rest shouldBe Empty
    }

    "seq() should return Empty" {
      val emptyList = l<Int>()

      emptyList.seq() shouldBeSameInstanceAs Empty
    }
  }

  "PersistentList.Cons" - {
    "first should return the head of the list" {
      checkAll { i: Int ->
        val cons = Cons(i, Empty)

        cons.first.shouldNotBeNull()
        cons.first.shouldBeExactly(i)
      }
    }

    "rest should return a Seq" {
      val list1 = Cons(10, Empty)
      val list2 = Cons(11, list1)
      val list3 = Cons(11, list2)

      list1.rest() shouldBeSameInstanceAs Empty
      list2.rest() shouldBeSameInstanceAs list1
      list3.rest() shouldBeSameInstanceAs list2
    }

    "toString()" {
      PersistentList(1).toString() shouldBe "(1)"
      PersistentList(1, 2, 3, 4).toString() shouldBe "(1 2 3 4)"
    }

    "count" {
      PersistentList(3, 45).count shouldBeExactly 2
      PersistentList(3, 34, 45).count shouldBeExactly 3
    }

    "empty()" {
      val list = PersistentList(1, 2, 3)

      list.empty() shouldBe Empty
    }

    "seq() should return null" {
      val list = l(1, 2, 3)

      val seq = list.seq()

      seq shouldBeSameInstanceAs list
    }

    "implementation of List_E" - {
      "size()" {
        val list = PersistentList(1, 2, 34, 4)
        list.size shouldBeExactly 4
      }

      "contains(element)" {
        val list = PersistentList(1, 2, 3, 4L, 1.4f)

        list.contains(4).shouldBeTrue()
        list.contains(10).shouldBeFalse()
        Empty.contains(0).shouldBeFalse()
      }

      "containsAll(Collection)" {
        val list = PersistentList(1, 2, 3, 4L, 1.4f)

        list.containsAll(listOf(3, 2, 4L)).shouldBeTrue()
        list.containsAll(listOf(3, 2, 0, 1, 4)).shouldBeFalse()
      }

      "indexOf(element)" {
        val list = PersistentList(1L, 2.0, 3, 4)

        list.indexOf(3) shouldBeExactly 2
        list.indexOf(4L) shouldBeExactly 3
        list.indexOf(1) shouldBeExactly 0
        list.indexOf(6) shouldBeExactly -1
        Empty.indexOf(6) shouldBeExactly -1
      }

      "isEmpty() should return false" {
        Cons(1, Empty).isEmpty().shouldBeFalse()
      }

      "get(index)" - {
        val ints = listOf(1, 2, 3, 4, 5, 6)
        val list = PersistentList(*ints.toTypedArray())

        "when index is valid, it should return an element by index" {
          for ((index, value) in ints.withIndex()) {
            list[index] shouldBeExactly value
          }
        }

        """when index is out of bounds, it should throw
                   IndexOutOfBoundsException in constant-time""" {
          val index1 = 10
          val index2 = -5
          val e1 = shouldThrowExactly<IndexOutOfBoundsException> {
            list[index1]
          }
          val e2 = shouldThrowExactly<IndexOutOfBoundsException> {
            list[index2]
          }

          e1.message shouldBe "index = $index1"
          e2.message shouldBe "index = $index2"
        }
      }

      "iterator()" {
        val list: PersistentList<Int> = Cons(1, Empty)

        val seqIter = list.iterator() as SeqIterator

        seqIter.next shouldBe list
        seqIter.next shouldBeSameInstanceAs list
      }

      "lastIndexOf(element)" - {
        val list = PersistentList(1, 1, 6, 6, 4, 5, 4)

        "when the element is not in the list, it should return -1" {
          list.lastIndexOf(10) shouldBeExactly -1
          Empty.lastIndexOf(10) shouldBeExactly -1
        }

        """|when the element is in the list,
                   |it should return the index of the last occurrence
                   |of the specified element
                """ {
          list.lastIndexOf(6) shouldBeExactly 3
          list.lastIndexOf(1) shouldBeExactly 1
          list.lastIndexOf(4) shouldBeExactly 6
        }
      }

      "listIterator()" {
        val list = PersistentList(1, 2, 3)
        val expected = list.toList().listIterator()

        val listIterator = list.listIterator()

        shouldThrowExactly<NoSuchElementException> {
          listIterator.previous()
        }
        listIterator.next() shouldBe expected.next()
        listIterator.next() shouldBe expected.next()
        listIterator.next() shouldBe expected.next()
        listIterator.hasNext() shouldBe expected.hasNext()
        listIterator.hasPrevious() shouldBe expected.hasPrevious()
        listIterator.nextIndex() shouldBe expected.nextIndex()
        listIterator.previousIndex() shouldBe expected.previousIndex()
        shouldThrowExactly<NoSuchElementException> {
          listIterator.next()
        }
      }

      "listIterator(index)" - {
        val list = PersistentList(1, 2, 3)

        "when index is valid, it should return a list iterator" {
          val index = 1
          val expect = list.toList().listIterator(index)

          val listIterator = list.listIterator(index)

          listIterator.previous() shouldBe expect.previous()
          listIterator.next() shouldBe expect.next()
          listIterator.next() shouldBe expect.next()
          listIterator.next() shouldBe expect.next()
          listIterator.hasNext() shouldBe expect.hasNext()
          listIterator.hasPrevious() shouldBe expect.hasPrevious()
          listIterator.nextIndex() shouldBe expect.nextIndex()
          listIterator.previousIndex() shouldBe expect.previousIndex()
          shouldThrowExactly<NoSuchElementException> {
            listIterator.next()
          }
        }

        "when index is out of bounds, it should throw" {
          shouldThrowExactly<IndexOutOfBoundsException> {
            list.listIterator(4)
          }
        }
      }

      "subList(start, end)" - {
        val list = PersistentList(1, 2, 3, 4)

        "when passing valid indices, it returns a sublist of this" {
          val expectedSub = listOf(2, 3)

          val sublist = list.subList(1, 3)

          sublist.size shouldBe 2
          sublist shouldBe expectedSub
        }

        "when passing out of bounds indices, it should throw" {

          shouldThrowExactly<IndexOutOfBoundsException> {
            list.subList(-1, 3)
          }
          shouldThrowExactly<IndexOutOfBoundsException> {
            list.subList(1, 6)
          }
          shouldThrowExactly<IndexOutOfBoundsException> {
            list.subList(-1, 6)
          }
        }
      }
    }
  }

  "l(args)" - {
    "l() without args should return an empty PersistentList" {
      l<Any>() shouldBe Empty
    }

    "l(args) with args should return a PersistentList" {
      l(1, 2, 3, 4) shouldBe PersistentList(1, 2, 3, 4)
    }
  }

  "toPlist()" {
    listOf(1, 2, 3).toPlist() shouldBe l(1, 2, 3)
    listOf<Int>().toPlist() shouldBe l()
  }
})
