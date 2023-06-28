package io.github.yahyatinani.y.core.collections.vector

import io.github.yahyatinani.y.core.assertArraysAreEquiv
import io.github.yahyatinani.y.core.collections.APersistentVector
import io.github.yahyatinani.y.core.collections.ArrayChunk
import io.github.yahyatinani.y.core.collections.BF
import io.github.yahyatinani.y.core.collections.Edit
import io.github.yahyatinani.y.core.collections.IMapEntry
import io.github.yahyatinani.y.core.collections.ISeq
import io.github.yahyatinani.y.core.collections.PersistentList
import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.collections.PersistentVector
import io.github.yahyatinani.y.core.collections.PersistentVector.ChunkedSeq
import io.github.yahyatinani.y.core.collections.PersistentVector.EmptyVector
import io.github.yahyatinani.y.core.collections.PersistentVector.Node
import io.github.yahyatinani.y.core.collections.PersistentVector.TransientVector
import io.github.yahyatinani.y.core.collections.SHIFT
import io.github.yahyatinani.y.core.l
import io.github.yahyatinani.y.core.mocks.MockSeq
import io.github.yahyatinani.y.core.mocks.User
import io.github.yahyatinani.y.core.mocks.VectorMock
import io.github.yahyatinani.y.core.toPlist
import io.github.yahyatinani.y.core.util.HASH_PRIME
import io.github.yahyatinani.y.core.util.Murmur3
import io.github.yahyatinani.y.core.util.hasheq
import io.github.yahyatinani.y.core.v
import io.github.yahyatinani.y.core.vec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

class PersistentVectorTest : FreeSpec({
  "Node" - {
    @Suppress("UNCHECKED_CAST")
    "Node does have an array of nodes" {
      val array = arrayOfNulls<Int>(33)

      val node = Node<Int>(Edit(null), array as Array<Any?>)

      node.array shouldBeSameInstanceAs array
    }

    "Empty node" {
      val emptyNode = Node.EmptyNode
      val nodes = emptyNode.array

      nodes.size shouldBeExactly 32
      nodes[0].shouldBeNull()
      nodes[31].shouldBeNull()
    }
  }

  "PersistentVector" - {
    "invoke() should return the EmptyVector" {
      PersistentVector<Int>() shouldBe EmptyVector
    }

    "invoke(args)" - {
      "when args count <= 32, it should add to the tail" {
        checkAll(Arb.list(Arb.int(), 0..32)) { list: List<Int> ->
          val vec = PersistentVector(*list.toTypedArray())
          val tail = vec.tail

          if (vec.count == 0) {
            vec shouldBeSameInstanceAs EmptyVector
          }
          vec.shift shouldBeExactly SHIFT
          vec.count shouldBeExactly list.size
          vec.root shouldBeSameInstanceAs Node.EmptyNode
          tail.size shouldBeExactly list.size
          tail shouldContainAll list
        }
      }

      "when args count > 32, it should call conj" {
        val list = (1..100).toList()

        val vec = PersistentVector(*list.toTypedArray())
        val tail = vec.tail
        val root = vec.root

        vec.shift shouldBeExactly SHIFT
        vec.count shouldBeExactly list.size
        tail.size shouldBeExactly 4
        root.array[0].shouldNotBeNull()
        root.array[1].shouldNotBeNull()
        root.array[2].shouldNotBeNull()
        root.array[3].shouldBeNull()
        tail[0] shouldBe 97
        tail[1] shouldBe 98
        tail[2] shouldBe 99
        tail[3] shouldBe 100
      }
    }

    "length()" {
      PersistentVector(2, 3, 45, 5).length() shouldBeExactly 4
    }

    "conj(e)" - {
      val i = 7
      "when level is 5 and there is room in tail, it should add to it" {
        val ints = Arb.int().filter { it != i }
        checkAll(Arb.list(ints, 0..31)) { list ->
          val tempVec = PersistentVector(*list.toTypedArray())

          val vec = tempVec.conj(i)
          val tail = vec.tail
          val root = vec.root

          vec.shift shouldBeExactly SHIFT
          vec.count shouldBeExactly list.size + 1
          root shouldBeSameInstanceAs Node.EmptyNode
          root.edit.value.shouldBeNull()
          root.edit shouldBeSameInstanceAs tempVec.root.edit
          tail.size shouldBeExactly list.size + 1
          tail[list.size] as Int shouldBeExactly i
        }
      }

      @Suppress("UNCHECKED_CAST")
      "when the tail is full, it should push tail into the vec" - {
        "when the level is 5, it should insert the tail in root node" {
          val listGen = Arb.list(Arb.int(), (32..1024)).filter {
            it.size % 32 == 0
          }

          checkAll(listGen, Arb.int()) { l: List<Int>, i: Int ->
            val tempVec = PersistentVector(*l.toTypedArray())
            val tempTail = tempVec.tail

            val vec = tempVec.conj(i)
            val tail = vec.tail

            vec.shift shouldBeExactly SHIFT
            var index = 31
            var isMostRightLeafFound = false
            while (index >= 0 && !isMostRightLeafFound) {
              val o = vec.root.array[index]
              if (o != null) {
                val mostRightLeaf = o as Node<Int>
                val array = mostRightLeaf.array
                array shouldBeSameInstanceAs tempTail
                mostRightLeaf.edit shouldBeSameInstanceAs
                  tempVec.root.edit
                isMostRightLeafFound = true
              }
              index--
            }

            vec.root.edit.value.shouldBeNull()
            isMostRightLeafFound.shouldBeTrue()
            vec.count shouldBeExactly l.size + 1
            tail.size shouldBeExactly 1
            tail[0] shouldBe i
          }
        }

        """when the level is > 5, it should iterate through the
                                            levels then insert the tail""" {
          val e = 99
          val list = (1..1088).toList()
          val tempVec = PersistentVector(*list.toTypedArray())
          val tempTail = tempVec.tail

          val vec = tempVec.conj(e)
          val root = vec.root
          val mostRightLeaf = (
            (root.array[1] as Node<Int>).array[1] as Node<Int>
            ).array

          vec.shift shouldBeExactly SHIFT * 2
          mostRightLeaf shouldBeSameInstanceAs tempTail
          vec.tail[0] shouldBe e
          vec.count shouldBeExactly list.size + 1
          root.edit shouldBeSameInstanceAs tempVec.root.edit
          root.edit.value.shouldBeNull()
        }

        """when the level is > 5 and the path is null,
                    it should create a new path then insert the tail""" {
          val e = 99
          val list = (1..2080).toList()
          val tempVec = PersistentVector(*list.toTypedArray())
          val tempTail = tempVec.tail
          val tempEdit = tempVec.root.edit

          val vec = tempVec.conj(e)
          val root = vec.root
          val subRoot = root.array[2] as Node<Int>
          val mostRightLeaf = subRoot.array[0] as Node<Int>

          vec.shift shouldBeExactly SHIFT * 2
          mostRightLeaf.array shouldBeSameInstanceAs tempTail
          vec.tail[0] shouldBe e
          vec.count shouldBeExactly list.size + 1
          root.edit.value.shouldBeNull()
          root.edit shouldBeSameInstanceAs tempEdit
          subRoot.edit.value.shouldBeNull()
          subRoot.edit shouldBeSameInstanceAs tempEdit
          mostRightLeaf.edit.value.shouldBeNull()
          mostRightLeaf.edit shouldBeSameInstanceAs tempEdit
        }

        "when root overflow, it should add 1 lvl by creating new root" {
          val e = 99
          val list = (1..1056).toList()
          val tempVec = PersistentVector(*list.toTypedArray())
          val tempEdit = tempVec.root.edit

          val vec = tempVec.conj(e)
          val root = vec.root
          val subRoot1 = root.array[0] as Node<Int>
          val subRoot2 = root.array[1] as Node<Int>
          val mostRightLeaf = subRoot2.array[0] as Node<Int>

          vec.count shouldBeExactly list.size + 1
          vec.shift shouldBeExactly 10
          vec.tail[0] as Int shouldBeExactly e
          mostRightLeaf.array shouldBeSameInstanceAs tempVec.tail

          root.edit.value.shouldBeNull()
          root.edit shouldBeSameInstanceAs tempEdit
          subRoot1.edit shouldBeSameInstanceAs tempEdit
          subRoot2.edit shouldBeSameInstanceAs tempEdit
        }
      }
    }

    "assocN(index, val)" - {

      "when index out of bounds, it should throw an exception" {
        val vec = PersistentVector(1, 2, 3, 4)

        shouldThrowExactly<IndexOutOfBoundsException> {
          vec.assocN(10, 15)
        }
      }

      "when index equals the count of the vector, it should conj" {
        val vec = PersistentVector(1, 2, 3, 4)
        val index = vec.count
        val value = 15

        val rvec = vec.assocN(index, value)

        rvec shouldNotBeSameInstanceAs vec
        rvec.count shouldBeExactly vec.count + 1
        rvec.nth(index) shouldBeExactly value
        vec.fold(0) { i: Int, n: Int ->
          rvec.nth(i) shouldBeExactly n
          i + 1
        }
      }

      "when index within vec bounds, update the associate value" - {
        "when the vec is empty, it should conj the value" {
          val value = 15
          val vec = v<Int>()

          val rvec = vec.assocN(0, value)

          rvec shouldNotBeSameInstanceAs vec
          rvec.count shouldBeExactly 1
          rvec.nth(0) shouldBeExactly value
        }

        "when index within the tail" {
          val vec = PersistentVector(1, 2, 3, 4)
          val index1 = vec.count - 1
          val index2 = 0
          val value = 15

          val rvec1 = vec.assocN(index1, value)
          val rvec2 = vec.assocN(index2, value)

          rvec2.nth(index2) shouldBeExactly value
          rvec1 shouldNotBeSameInstanceAs vec
          rvec1.count shouldBeExactly vec.count
          rvec1.nth(index1) shouldBeExactly value
          vec.fold(0) { i: Int, n: Int ->
            when (i) {
              index1 -> rvec1.nth(i) shouldBeExactly value
              else -> rvec1.nth(i) shouldBeExactly n
            }
            i + 1
          }
        }

        "when index within the tree and level 5" {
          val vec = PersistentVector(*(1..50).toList().toTypedArray())
          val index = 30
          val value = 99

          val rvec = vec.assocN(index, value)

          rvec shouldNotBeSameInstanceAs vec
          rvec.nth(index) shouldBeExactly value
          rvec.count shouldBeExactly vec.count

          vec.nth(index) shouldBeExactly 31
        }
      }
    }

    "empty()" {
      v(
        1,
        2,
        3,
        4,
      ).empty() shouldBeSameInstanceAs EmptyVector
    }

    val default = -1
    "nth(index)" {
      val list = (1..1056).toList()

      val vec = PersistentVector(*list.toTypedArray())

      shouldThrowExactly<IndexOutOfBoundsException> { vec.nth(2000) }
      shouldThrowExactly<IndexOutOfBoundsException> { vec.nth(list.size) }
      shouldThrowExactly<IndexOutOfBoundsException> { vec.nth(default) }
      vec.nth(1055) shouldBeExactly 1056
      vec.nth(1024) shouldBeExactly 1025
      vec.nth(1023) shouldBeExactly 1024
    }

    "nth(index, default)" {
      val list = (1..1056).toList()

      val vec = PersistentVector(*list.toTypedArray())

      vec.nth(1055, default) shouldBeExactly 1056
      vec.nth(1024, default) shouldBeExactly 1025
      vec.nth(1023, default) shouldBeExactly 1024
      vec.nth(2000, default) shouldBeExactly default
      vec.nth(default, default) shouldBeExactly default
    }

    "count" {
      PersistentVector(45, 2, 3).count shouldBeExactly 3
    }

    "toString()" {
      val vec = PersistentVector(1, 2, 3, 4)

      vec.toString() shouldBe "[1 2 3 4]"
    }

    "it's root should be immutable" {
      val vec = PersistentVector(1, 2, 3)

      vec.root.edit.value.shouldBeNull()
    }

    "asTransient() should turn this vector into a transient vector" {
      val vec = PersistentVector(1, 2, 3, 4, 5, 6, 7, 8)

      val tVec: TransientVector<Int> = vec.asTransient()

      tVec.count shouldBeExactly vec.count
      tVec.shift shouldBeExactly vec.shift
      tVec.root.edit.shouldNotBeNull()
      assertArraysAreEquiv(tVec.tail, vec.tail)
    }

    "seq()" - {
      "when called on an empty vector, it should return Empty" {
        val emptyVec = v<Int>()

        emptyVec.seq() shouldBeSameInstanceAs Empty
      }

      "when called on a filled vector, it should return a ChunkedSeq" {
        val vec = PersistentVector(1, 2, 3)

        val chunkedSeq = vec.seq() as ChunkedSeq<Int>

        chunkedSeq.vector shouldBeSameInstanceAs vec
        chunkedSeq.index shouldBeExactly 0
        chunkedSeq.offset shouldBeExactly 0
        chunkedSeq.node shouldBeSameInstanceAs vec.leafArrayBy(0)
      }
    }

    "hashCode()" - {
      "when called on EmptyVector, it should return 1" {
        EmptyVector.hashCode() shouldBeExactly 1
      }

      "when called on a populated vector it should calculate the hash" {
        val list = (1..20).toList()
        val prime = 31
        val expectedHash = list.fold(1) { hash, i ->
          prime * hash + i.hashCode()
        }
        val vec = PersistentVector(*list.toTypedArray())

        vec.hashCode() shouldBeExactly expectedHash
        vec.hashCode shouldBeExactly expectedHash

        EmptyVector.hashCode() shouldBeExactly 1
        EmptyVector.hashCode shouldBeExactly 1
      }
    }

    "equals(x)" {
      v(1, 2, 3, 4).equals(null).shouldBeFalse()

      (v(1) == PersistentVector(1, 2, 3)).shouldBeFalse()

      (v(1, 2, 3) == PersistentVector(1, 2, 3)).shouldBeTrue()

      val vector = PersistentVector(1, 2, 3)
      (vector == vector).shouldBeTrue()

      (v(1, 2, 3) == PersistentVector(1, 2, 5)).shouldBeFalse()

      (vector(vector(1)) == vector(vector(1))).shouldBeTrue()

      (v(1, 2, 3) == listOf(1, 4)).shouldBeFalse()

      (v(1, 2, 3) == listOf(1, 2, 5)).shouldBeFalse()

      (v(1, 2, 3) == listOf(1, 2, 3)).shouldBeTrue()

      (v(User(1)) == listOf(User(2))).shouldBeFalse()

      (v(1, 2) == mapOf(1 to 2)).shouldBeFalse()

      (v(1, 2) == MockSeq(v(1, 2))).shouldBeTrue()

      (v(1, 2) == MockSeq(v(1, 3))).shouldBeFalse()

      (v(1, 2) == MockSeq(v(1, 2, 4))).shouldBeFalse()
    }

    "equiv(x)" {
      // assert equals behaviour
      v(1, 2, 3, 4).equiv(null).shouldBeFalse()

      v(1).equiv(v(1, 2, 3)).shouldBeFalse()

      v(1, 2, 3).equiv(v(1, 2, 3)).shouldBeTrue()

      v(1, 2, 3).equiv(v(1, 2, 5)).shouldBeFalse()

      v(v(1)).equiv(v(v(1))).shouldBeTrue()

      v(1, 2, 3).equiv(listOf(1, 4)).shouldBeFalse()

      v(1, 2, 3).equiv(listOf(1, 2, 5)).shouldBeFalse()

      v(1, 2, 3).equiv(listOf(1, 2, 3)).shouldBeTrue()

      v(User(1)).equiv(listOf(User(2))).shouldBeFalse()

      v(1, 2).equiv(mapOf(1 to 2)).shouldBeFalse()

      v(1, 2).equiv(MockSeq(v(1, 2))).shouldBeTrue()

      v(1, 2).equiv(MockSeq(v(1, 3))).shouldBeFalse()

      v(1, 2).equiv(MockSeq(v(1, 2, 4))).shouldBeFalse()

      // assert equiv behaviour
      v(1).equiv("vec").shouldBeFalse()

      v(1).equiv(l(2, null)).shouldBeFalse()

      v(2, null).equiv(l(2, 3)).shouldBeFalse()

      v(null, 2).equiv(l(2, 3)).shouldBeFalse()

      v(1).equiv(setOf(1)).shouldBeFalse()

      v(Any()).equiv(v(Any())).shouldBeFalse()

      v(1).equiv(v(1L)).shouldBeTrue()

      v(l(1)).equiv(PersistentList(listOf(1L))).shouldBeTrue()

      v(listOf(1L)).equiv(l(l(1))).shouldBeTrue()

      v(1.1).equiv(l(1.1)).shouldBeTrue()
    }

    "hasheq()" {
      val vec = PersistentVector(1, 2, 3, 4)
      val h = vec.fold(1) { hash: Int, i: Int ->
        (HASH_PRIME * hash) + hasheq(i)
      }
      val expectedHash = Murmur3.mixCollHash(h, vec.count)

      vec.hasheq() shouldBeExactly expectedHash
      vec.hasheq shouldBeExactly expectedHash
    }

    "valAt(key, default)" {
      val vec = PersistentVector(1, 2, 3, 4)

      vec.valAt(4, default) shouldBe default
      vec.valAt(0, default) shouldBe 1
      vec.valAt(2, default) shouldBe 3
    }

    "valAt(key)" {
      val vec = PersistentVector(1, 2, 3, 4)

      vec.valAt(4) shouldBe null
      vec.valAt(0) shouldBe 1
      vec.valAt(2) shouldBe 3
    }

    "containsKey(key)" {
      val vec = PersistentVector(1, 2, 3, 4)

      vec.containsKey(0).shouldBeTrue()
      vec.containsKey(10).shouldBeFalse()
    }

    "entryAt(key)" {
      val vec = PersistentVector(1, 2, 3, 4)

      val entry = vec.entryAt(2) as IMapEntry<Int, Int>

      entry.key shouldBeExactly 2
      entry.value shouldBeExactly 3

      vec.entryAt(6).shouldBeNull()
    }

    "assoc(key, value)" {
      val vec = PersistentVector(1, 2, 3, 4)

      vec.assoc(0, 74).nth(0) shouldBe 74
      vec.assoc(1, 73).nth(1) shouldBe 73
      vec.assoc(2, 56).nth(2) shouldBe 56
      val e = shouldThrowExactly<IndexOutOfBoundsException> {
        vec.assoc(20, 177)
      }

      e.message shouldBe "index = 20"
    }

    "subvec(start, end)" {
      val start = 1
      val end = 5
      val vec = PersistentVector(1, 2, 3, 4, 5, 6)

      val subvec =
        vec.subvec(start, end) as APersistentVector.SubVector<Int>

      subvec.vec shouldBeSameInstanceAs vec
      subvec.start shouldBeExactly start
      subvec.end shouldBeExactly end
    }

    "compareTo(other)" - {
      "when this.count < other.count, it should return -1" {
        val vec1 = PersistentVector(1, 2, 3)
        val vec2 = PersistentVector(1, 2, 3, 4)

        vec1.compareTo(vec2) shouldBeExactly -1
      }

      "when this count > other count, it should return 1" {
        val vec1 = PersistentVector(1, 2, 3, 4)
        val vec2 = PersistentVector(1, 2, 3)

        vec1.compareTo(vec2) shouldBeExactly 1
      }

      "when this count == other count" - {

        "when all items are equal, it should return 0" {
          PersistentVector(1, 2, 3)
            .compareTo(v(1, 2, 3)) shouldBeExactly 0

          PersistentVector<Number>(1L, 2)
            .compareTo(v(1.0, 2)) shouldBeExactly 0

          PersistentVector<Any>(v(1, 2))
            .compareTo(v(v(1L, 2.0))) shouldBeExactly 0
        }

        "when this items < than other's, it should return -1" {
          PersistentVector(null, 2, 3)
            .compareTo(v(1, 2, 3)) shouldBeExactly -1

          PersistentVector<Number>(1L, 2)
            .compareTo(v(1.1, 2)) shouldBeExactly -1
        }

        "when this items > than other's, it should return 1" {
          PersistentVector<Int?>(1, 2, 3)
            .compareTo(v(null, 2, 3)) shouldBeExactly 1

          PersistentVector<Number>(1.1, 2)
            .compareTo(v(1L, 2)) shouldBeExactly 1

          PersistentVector<Any>(v(1.1, 2))
            .compareTo(v(v(1L, 2.0))) shouldBeExactly 1
        }
      }
    }

    "invoke(index) should return the associate value of the given index" {
      val vec = PersistentVector(1, 2, 3, 4)

      vec(0) shouldBeExactly 1
      vec(1) shouldBeExactly 2
      vec(3) shouldBeExactly 4
      shouldThrowExactly<IndexOutOfBoundsException> { vec.nth(2000) }
    }

    "reverse()" - {
      "when the the vec is empty, it should return the empty seq" {
        val rseq: ISeq<Int> = PersistentVector<Int>().reverse()

        rseq shouldBeSameInstanceAs Empty
      }

      "when vec is populated, it should return the reversed seq of it " {
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val rseq = vec.reverse() as APersistentVector.RSeq<Int>

        rseq.vec shouldBeSameInstanceAs vec
        rseq.index shouldBeExactly vec.count - 1
        rseq.count shouldBeExactly vec.count
      }
    }

    "peek() should return the last element in the vector" {
      v<Int>().peek().shouldBeNull()
      v(1, 2, 3).peek() shouldBe 3
      v(1, 2, 3, 4).peek() shouldBe 4
      v(1, 2, 3, 4, 5).peek() shouldBe 5
    }

    "pop()" - {
      "when the vec count == 1 or 0, it should return the EmptyVector" {
        v<Int>().pop() shouldBeSameInstanceAs EmptyVector
        v(5).pop() shouldBeSameInstanceAs EmptyVector
      }

      "when tail length > 1, it should remove the last element in it" {
        checkAll(Arb.list(Arb.int(), 2..32)) { list: List<Int> ->
          val v =
            PersistentVector(*list.toTypedArray())

          val vec = v.pop()

          vec.count shouldBeExactly v.count - 1
          vec.shift shouldBeExactly v.shift
          vec.root shouldBeSameInstanceAs v.root
          vec.tail.size shouldBeExactly v.count - 1
          vec(v.count - 2) shouldBeExactly v.nth(v.count - 2)
        }
      }

      "when tail length == 1" - {
        """when level = 5 & root contains only 1 element after popping,
                   root should become the empty node""" {
          val v = PersistentVector(*(1..33).toList().toTypedArray())

          val vec = v.pop()

          vec.count shouldBeExactly v.count - 1
          vec.shift shouldBeExactly v.shift
          vec.root shouldBeSameInstanceAs Node.EmptyNode
          vec.tail.size shouldBeExactly v.count - 1
          vec(v.count - 2) shouldBeExactly v.nth(v.count - 2)
        }

        """when level = 5 & root contains more than 1 element after
                   popping, it should set the rightmost leaf to null""" {
          val v = PersistentVector(*(1..65).toList().toTypedArray())

          val vec = v.pop()
          val root = vec.root

          vec.shift shouldBeExactly 5
          vec.count shouldBeExactly v.size - 1
          vec.tail.size shouldBeExactly 32
          vec.nth(0) shouldBeExactly 1
          vec.tail[31] shouldBe 64

          root.edit shouldBeSameInstanceAs v.root.edit
          root.array[1].shouldBeNull()
        }

        """when level > 5 & root contains only 1 element after
                   popping, it should decrease level and eliminate the empty
                   node""" {
          val v = PersistentVector(*(1..1057).toList().toTypedArray())

          val vec = v.pop()
          val root = vec.root

          vec.shift shouldBeExactly 5
          vec.count shouldBeExactly v.size - 1
          vec.tail.size shouldBeExactly 32
          root.edit shouldBeSameInstanceAs v.root.edit
          root.array.fold(Unit) { _: Unit, element: Any? ->
            element.shouldNotBeNull()
          }
        }

        """when level > 10 & root contains only 1 element after
                   popping, it should decrease level and eliminate the
                   empty node""" {
          val v =
            PersistentVector(*(1..32801).toList().toTypedArray())

          val vec = v.pop()
          val root = vec.root

          vec.shift shouldBeExactly 10
          vec.count shouldBeExactly v.size - 1
          vec.tail.size shouldBeExactly 32
          root.edit shouldBeSameInstanceAs v.root.edit
          root.array.fold(Unit) { _: Unit, element: Any? ->
            element.shouldNotBeNull()
          }
        }
      }
    }

    "+ operator should act like conj" {
      val vec = PersistentVector(1, 2, 3, 4)

      val r = vec + 5

      r.count shouldBeExactly vec.count + 1
      r.nth(4) shouldBeExactly 5
    }

    "ChunkedSeq" - {
      "firstChunk()" {
        val vec = PersistentVector(1, 2, 3)
        val chunkedSeq = ChunkedSeq(vec, 0, 0)
        val node = vec.leafArrayBy(0)

        val firstChunk = chunkedSeq.firstChunk() as ArrayChunk

        firstChunk.array shouldBeSameInstanceAs node
        firstChunk.start shouldBeExactly chunkedSeq.offset
        firstChunk.end shouldBeExactly node.size
      }

      "restChunks()" {
        val index = 0
        val offset = 0
        val vec = PersistentVector(*(0..45).toList().toTypedArray())
        val chunkedSeq = ChunkedSeq(vec, index, offset)
        val node = vec.leafArrayBy(0)

        val restChunks =
          chunkedSeq.restChunks() as ChunkedSeq<Int>

        restChunks.vector shouldBeSameInstanceAs vec
        restChunks.index shouldBeExactly index + node.size
        restChunks.offset shouldBeExactly offset

        restChunks.restChunks() shouldBeSameInstanceAs Empty
      }

      "first()" {
        val vec = PersistentVector(*(0..45).toList().toTypedArray())
        val chunkedSeq = ChunkedSeq(vec, 0, 0)

        chunkedSeq.first() shouldBeExactly 0
        chunkedSeq.restChunks().first() shouldBeExactly 32
      }

      "rest()" {
        val vec = PersistentVector(*(0..45).toList().toTypedArray())
        val chunkedSeq = ChunkedSeq(vec, 0, 0)

        val rest = chunkedSeq.rest() as ChunkedSeq<Int>
        var nextChunk = rest
        var i = 1
        while (i <= 31) {
          nextChunk =
            nextChunk.rest() as ChunkedSeq<Int>
          i++
        }

        rest.first() shouldBeExactly 1
        rest.vector shouldBeSameInstanceAs vec
        rest.node shouldBeSameInstanceAs chunkedSeq.node
        rest.index shouldBeExactly chunkedSeq.index
        rest.offset shouldBeExactly chunkedSeq.offset + 1

        nextChunk.first() shouldBeExactly 32
        nextChunk.vector shouldBeSameInstanceAs vec
        nextChunk.index shouldBeExactly rest.index + rest.node.size
        nextChunk.node shouldBeSameInstanceAs
          vec.leafArrayBy(nextChunk.index)
        nextChunk.offset shouldBeExactly 0
      }

      "count" {
        val vec = PersistentVector(*(0..45).toList().toTypedArray())
        val chunkedSeq = ChunkedSeq(vec, 0, 0)

        val rest = chunkedSeq.rest() as ChunkedSeq<Int>
        var nextChunk = rest
        var i = 1
        while (i <= 31) {
          nextChunk =
            nextChunk.rest() as ChunkedSeq<Int>
          i++
        }

        rest.count shouldBeExactly vec.size - 1

        nextChunk.count shouldBeExactly 14
      }
    }

    "List implementation" - {
      "size()" {
        PersistentVector(13).size shouldBeExactly 1
        PersistentVector(2, 234, 43, 44).size shouldBeExactly 4
      }

      "get()" {
        val genA = Arb.list(Arb.int()).filter { it.isNotEmpty() }
        checkAll(genA) { l: List<Int> ->
          val vec = PersistentVector(*l.toTypedArray())

          val list = vec as List<Int>

          list[0] shouldBeExactly l[0]
        }
      }

      "isEmpty()" {
        PersistentVector<Int>().isEmpty().shouldBeTrue()

        PersistentVector(1, 2, 3, 4).isEmpty().shouldBeFalse()
      }

      "iterator()" {
        val list = listOf(1, 2, 34, 4, 5)
        val vec = PersistentVector(*list.toTypedArray())

        val iter = vec.iterator()

        iter.hasNext().shouldBeTrue()
        list.fold(Unit) { _: Unit, i: Int ->
          iter.next() shouldBeExactly i
        }

        iter.hasNext().shouldBeFalse()
        shouldThrowExactly<NoSuchElementException> { iter.next() }
      }

      "indexOf(element)" {
        val vec = PersistentVector(1L, 2.0, 3, 4)

        vec.indexOf(3) shouldBeExactly 2
        vec.indexOf(4L) shouldBeExactly 3
        vec.indexOf(1) shouldBeExactly 0
        vec.indexOf(6) shouldBeExactly -1
      }

      "lastIndexOf(element)" - {
        val vec = PersistentVector(1, 1, 6, 6, 4, 5, 4)

        "when the element is not in the list, it should return -1" {
          vec.lastIndexOf(10) shouldBeExactly -1
        }

        """|when the element is in the list,
                   |it should return the index of the last occurrence of
                   |the specified element
                """ {
          vec.lastIndexOf(6) shouldBeExactly 3
          vec.lastIndexOf(1) shouldBeExactly 1
          vec.lastIndexOf(4) shouldBeExactly 6
        }
      }

      "listIterator(index)" {
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val iter = vec.listIterator(2)

        iter.hasPrevious().shouldBeTrue()
        iter.hasNext().shouldBeTrue()

        iter.nextIndex() shouldBeExactly 2
        iter.previousIndex() shouldBeExactly 1

        iter.next() shouldBeExactly vec[2]

        iter.previousIndex() shouldBeExactly 2
        iter.nextIndex() shouldBeExactly 3

        iter.previous() shouldBeExactly vec[2]

        iter.previousIndex() shouldBeExactly 1
        iter.nextIndex() shouldBeExactly 2

        iter.next() shouldBeExactly vec[2]
        iter.next() shouldBeExactly vec[3]
        iter.next() shouldBeExactly vec[4]

        iter.hasNext().shouldBeFalse()

        shouldThrowExactly<NoSuchElementException> {
          iter.next()
        }

        iter.previous()
        iter.previous()
        iter.previous()
        iter.previous()
        iter.previous()

        iter.hasPrevious().shouldBeFalse()

        shouldThrowExactly<NoSuchElementException> {
          iter.previous()
        }
      }

      "listIterator()" {
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val iter = vec.listIterator()

        iter.hasPrevious().shouldBeFalse()
        iter.hasNext().shouldBeTrue()

        iter.nextIndex() shouldBeExactly 0
        iter.previousIndex() shouldBeExactly -1

        iter.next() shouldBeExactly vec[0]

        iter.previousIndex() shouldBeExactly 0
        iter.nextIndex() shouldBeExactly 1

        iter.previous() shouldBeExactly vec[0]

        iter.previousIndex() shouldBeExactly -1
        iter.nextIndex() shouldBeExactly 0

        iter.next() shouldBeExactly vec[0]
        iter.next() shouldBeExactly vec[1]
        iter.next() shouldBeExactly vec[2]
        iter.next() shouldBeExactly vec[3]
        iter.next() shouldBeExactly vec[4]

        iter.hasNext().shouldBeFalse()

        shouldThrowExactly<NoSuchElementException> {
          iter.next()
        }

        iter.previous()
        iter.previous()
        iter.previous()
        iter.previous()
        iter.previous()

        iter.hasPrevious().shouldBeFalse()

        shouldThrowExactly<NoSuchElementException> {
          iter.previous()
        }
      }

      "contains(element)" {
        val vec = PersistentVector(1, 2, 3, 4L)

        vec.contains(1).shouldBeTrue()
        vec.contains(1L).shouldBeTrue()

        vec.contains(15).shouldBeFalse()
      }

      "containsAll(elements)" {
        val vec = PersistentVector(1, 2L, 3, 4, "a")

        vec.containsAll(PersistentVector(1, 2, "a")).shouldBeTrue()

        vec.containsAll(PersistentVector(1, 2, "b")).shouldBeFalse()
      }

      "subList(fromIndex, toIndex)" {
        val fromIndex = 1
        val toIndex = 5
        val vec = PersistentVector(1, 2, 3, 4, 5, 6)

        val subvec = vec.subList(
          fromIndex,
          toIndex,
        ) as APersistentVector.SubVector<Int>

        subvec.vec shouldBeSameInstanceAs vec
        subvec.start shouldBeExactly fromIndex
        subvec.end shouldBeExactly toIndex
      }
    }
  }

  "EmptyVector" - {
    "toString() should return []" {
      PersistentVector<Int>().toString() shouldBe "[]"
    }

    "count should be 0" {
      PersistentVector<Int>().count shouldBeExactly 0
    }

    "shift should be 5" {
      PersistentVector<Int>().shift shouldBeExactly SHIFT
    }

    "tail should be an empty array of size 0" {
      val tail = PersistentVector<Int>().tail

      tail.size shouldBeExactly 0
      shouldThrow<Exception> {
        tail[0]
      }
    }

    "root should be an empty node of size 32" {
      val array = PersistentVector<Int>().root.array

      array.size shouldBeExactly 32
      array[0].shouldBeNull()
      array[31].shouldBeNull()
    }

    "its root should be immutable" {
      val emptyVec = PersistentVector<Int>()

      emptyVec.root.edit.value.shouldBeNull()
    }
  }

  "SubVector" - {
    "invoke()/ctor" - {
      "when start & end is out of bounds, it should throw exception" {
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val e1 = shouldThrowExactly<IndexOutOfBoundsException> {
          APersistentVector.SubVector(vec, 3, 2)
        }

        e1.message shouldBe "Make sure that the start < end: 3 < 2!"

        val e2 = shouldThrowExactly<IndexOutOfBoundsException> {
          APersistentVector.SubVector(vec, -1, 2)
        }

        e2.message shouldBe "Make sure that the start >= 0: -1 >= 0!"

        val e3 = shouldThrowExactly<IndexOutOfBoundsException> {
          APersistentVector.SubVector(vec, 1, 7)
        }

        e3.message shouldBe "Make sure that the end <= count: 7 <= 5!"
      }

      "when end == start, it should return the EmptyVector" {
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val empty = APersistentVector.SubVector(vec, 2, 2)

        empty shouldBeSameInstanceAs EmptyVector
      }

      "when start & end are valid, it should return a SubVector" {
        val start = 1
        val end = 5
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val subvec = APersistentVector.SubVector(
          vec,
          start,
          end,
        ) as APersistentVector.SubVector<Int>

        subvec.vec shouldBeSameInstanceAs vec
        subvec.start shouldBeExactly start
        subvec.end shouldBeExactly end
      }
    }

    "count" {
      val start = 1
      val end = 5
      val vec = PersistentVector(1, 2, 3, 4, 5)

      val subvec = APersistentVector.SubVector(
        vec,
        start,
        end,
      ) as APersistentVector.SubVector<Int>

      subvec.count shouldBeExactly end - start
    }

    "empty()" {
      val start = 1
      val end = 5
      val vec = PersistentVector(1, 2, 3, 4, 5)

      APersistentVector.SubVector(vec, start, end)
        .empty() shouldBeSameInstanceAs
        EmptyVector
    }

    "nth(index)" {
      val vec = PersistentVector(1, 2, 3, 4, 5)
      val subvec = APersistentVector.SubVector(vec, 1, 5)

      shouldThrowExactly<IndexOutOfBoundsException> {
        subvec.nth(-1)
      }.message shouldBe "The index should be >= 0: -1"

      shouldThrowExactly<IndexOutOfBoundsException> { subvec.nth(4) }
      shouldThrowExactly<IndexOutOfBoundsException> { subvec.nth(5) }

      subvec.nth(0) shouldBeExactly 2
      subvec.nth(2) shouldBeExactly 4
    }

    "conj(e)" {
      val start = 1
      val end = 5
      val vec = PersistentVector(1, 2, 3, 4, 5)
      val subvec = APersistentVector.SubVector(vec, start, end)
      val e = 66

      val rsubvec = subvec.conj(e) as APersistentVector.SubVector<Int>
      val lastElement = rsubvec.nth(rsubvec.count - 1)

      rsubvec.start shouldBeExactly start
      rsubvec.end shouldBeExactly end + 1
      rsubvec.count shouldBeExactly subvec.count + 1
      rsubvec.nth(0) shouldBeExactly 2
      lastElement shouldBeExactly e

      vec.count shouldBeExactly 5
    }

    "assocN(index, value)" - {
      "when the index is out of bounds, it should throw an exception" {
        val vec = PersistentVector(1, 2, 3, 4, 5)
        val subvec = APersistentVector.SubVector(vec, 1, 5)

        shouldThrowExactly<IndexOutOfBoundsException> {
          subvec.assocN(5, 75)
        }.message shouldBe "Index 5 is out of bounds."
      }

      "when index == count, it should append the value to the end" {
        val vec = PersistentVector(1, 2, 3, 4, 5)
        val subvec = APersistentVector.SubVector(vec, 1, 5)

        val r = subvec.assocN(4, 75) as APersistentVector.SubVector<Int>

        r.nth(4) shouldBeExactly 75
      }

      "when 0 <= index < count, it should update the associate element" {
        val start = 1
        val end = 5
        val index = 3
        val value = 62
        val vec = PersistentVector(1, 2, 3, 4, 5)
        val subvec = APersistentVector.SubVector(vec, start, end)

        val r = subvec.assocN(
          index,
          value,
        ) as APersistentVector.SubVector<Int>

        r.nth(index) shouldBeExactly value
        r.start shouldBeExactly start
        r.end shouldBeExactly end
        r.count shouldBeExactly subvec.count
        vec.count shouldBeExactly 5
      }
    }

    @Suppress("UNCHECKED_CAST")
    "iterator()" - {
      "iterator of a subvec of APersistentVector" {
        val start = 1
        val end = 4
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val subvec = APersistentVector.SubVector(vec, start, end)

        val iter = (subvec as List<Int>).iterator()

        iter.hasNext().shouldBeTrue()

        var i = start
        while (i < end) {
          iter.next() shouldBeExactly vec[i]

          i++
        }

        iter.hasNext().shouldBeFalse()
        shouldThrowExactly<NoSuchElementException> { iter.next() }
      }

      "iterator of the subvec of a subvec of APersistentVector" {
        val start = 0
        val end = 3
        val vec = PersistentVector(1, 2, 3, 4, 5)
        val subvec = APersistentVector.SubVector(vec, 1, 4)
        val subSubVec = APersistentVector.SubVector(subvec, start, end)

        val iter = (subSubVec as List<Int>).iterator()

        iter.hasNext().shouldBeTrue()

        var i = start
        while (i < end) {
          iter.next() shouldBeExactly subvec.nth(i)
          i++
        }

        iter.hasNext().shouldBeFalse()
        shouldThrowExactly<NoSuchElementException> { iter.next() }
      }

      "when the inner vector is not a APersistentVector, return super" {
        val start = 1
        val end = 4
        val vec = PersistentVector(1, 2, 3, 4, 5)

        val subvec =
          APersistentVector.SubVector(VectorMock(vec), start, end)

        val iter = (subvec as List<Int>).iterator()

        iter.hasNext().shouldBeTrue()

        var i = start
        while (i < end) {
          iter.next() shouldBeExactly vec[i]

          i++
        }

        iter.hasNext().shouldBeFalse()
        shouldThrowExactly<NoSuchElementException> { iter.next() }
      }
    }

    "pop()" - {
      "when the subvec count > 1, it should decrease the end by 1" {
        val start = 1
        val end = 4
        val vec = PersistentVector(1, 2, 3, 4, 5)
        val subvec = APersistentVector.SubVector(
          vec,
          start,
          end,
        ) as APersistentVector.SubVector<Int>

        val popped = subvec.pop() as APersistentVector.SubVector<Int>

        popped.start shouldBeExactly start
        popped.vec shouldBeSameInstanceAs vec
        popped.end shouldBeExactly end - 1
      }

      "when the subvec count = 1, it should return the empty vector" {
        val start = 1
        val end = 2
        val vec = PersistentVector(1, 2, 3, 4, 5)
        val subvec = APersistentVector.SubVector(
          vec,
          start,
          end,
        ) as APersistentVector.SubVector<Int>

        subvec.pop() shouldBeSameInstanceAs EmptyVector
      }
    }
  }

  "invoke(seq) seq length is grater than 32" {
    val l: ISeq<Int> = (1..39).toList().toPlist()

    val vec: PersistentVector<Int> = PersistentVector.invoke(l)

    vec.count shouldBeExactly l.count
    vec.shift shouldBeExactly SHIFT
    (vec.root.array[0] as Node<*>).array shouldContainAll (1..32).toList()
    vec.tail.size shouldBeExactly 7
    vec[32] shouldBeExactly 33
  }

  "invoke(seq) seq length is 32" {
    val l: ISeq<Int> = (1..32).toList().toPlist()

    val vec: PersistentVector<Int> = PersistentVector.invoke(l)

    vec.count shouldBeExactly BF
    vec.shift shouldBeExactly SHIFT
    vec.tail.size shouldBeExactly BF
    vec[31] shouldBeExactly 32
  }

  "invoke(seq) seq length is less than 32" {
    val l: ISeq<Int> = (1..15).toList().toPlist()

    val vec: PersistentVector<Int> = PersistentVector.invoke(l)

    vec.count shouldBeExactly 15
    vec.shift shouldBeExactly SHIFT
    vec.tail.size shouldBeExactly 15
    vec[14] shouldBeExactly 15
  }

  "vec()" {
    vec<Any>(null) shouldBeSameInstanceAs EmptyVector

    vec(listOf(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

    vec<Any>(l(1, 2, "3", 4).seq()) shouldBe v(1, 2, "3", 4)

    vec<Any>(listOf<Any>(1, 2, "3", 4).asIterable() as Any) shouldBe
      v(1, 2, "3", 4)

    vec(arrayListOf(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

    vec(l(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

    vec<Any>(arrayOf(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

    vec<Int>(intArrayOf(1, 2, 4)) shouldBe v(1, 2, 4)

    vec<Short>(shortArrayOf(1, 2)) shouldBe v(1.toShort(), 2.toShort())

    vec<Byte>(byteArrayOf(1, 2)) shouldBe v(1.toByte(), 2.toByte())

    vec<Boolean>(booleanArrayOf(true, false)) shouldBe v(true, false)

    vec<Char>(charArrayOf('a', 'b')) shouldBe v('a', 'b')

    vec<Float>(floatArrayOf(1F, 2F)) shouldBe v(1F, 2F)

    vec<Double>(doubleArrayOf(1.0, 2.0)) shouldBe v(1.0, 2.0)

    vec<Long>(longArrayOf(1L, 2L)) shouldBe v(1L, 2L)

    shouldThrow<IllegalArgumentException> { vec<Long>(1) }
      .message shouldBe "Int can't be turned into a vec."
  }
})
