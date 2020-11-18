package com.github.whyrising.y

import com.github.whyrising.y.APersistentVector.RSeq
import com.github.whyrising.y.APersistentVector.Seq
import com.github.whyrising.y.APersistentVector.SubVector
import com.github.whyrising.y.PersistentList.Empty
import com.github.whyrising.y.PersistentVector.EmptyVector
import com.github.whyrising.y.PersistentVector.Node
import com.github.whyrising.y.PersistentVector.Node.EmptyNode
import com.github.whyrising.y.PersistentVector.TransientVector
import com.github.whyrising.y.map.IMapEntry
import com.github.whyrising.y.mocks.MockSeq
import com.github.whyrising.y.mocks.User
import com.github.whyrising.y.mocks.VectorMock
import com.github.whyrising.y.seq.ISeq
import com.github.whyrising.y.util.HASH_PRIME
import com.github.whyrising.y.util.INIT_HASH_CODE
import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.util.hasheq
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.merge
import io.kotest.property.checkAll
import kotlinx.atomicfu.atomic
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

const val SHIFT = 5

@ExperimentalSerializationApi
@ExperimentalStdlibApi
class PersistentVectorTest : FreeSpec({
    "Node" - {
        @Suppress("UNCHECKED_CAST")
        "Node does have an array of nodes" {
            val array = arrayOfNulls<Int>(33)

            val node = Node<Int>(atomic(false), array as Array<Any?>)

            node.array shouldBeSameInstanceAs array
        }

        "Empty node" {
            val emptyNode = EmptyNode
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

                    if (vec.count == 0)
                        vec shouldBeSameInstanceAs EmptyVector
                    vec.shift shouldBeExactly SHIFT
                    vec.count shouldBeExactly list.size
                    vec.root shouldBeSameInstanceAs EmptyNode
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
            checkAll { list: List<Int> ->
                val vec = PersistentVector(*list.toTypedArray())

                vec.length() shouldBeExactly list.size
            }
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
                    root shouldBeSameInstanceAs EmptyNode
                    root.isMutable.value.shouldBeFalse()
                    root.isMutable shouldBeSameInstanceAs tempVec.root.isMutable
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
                                mostRightLeaf.isMutable shouldBeSameInstanceAs
                                    tempVec.root.isMutable
                                isMostRightLeafFound = true
                            }
                            index--
                        }

                        vec.root.isMutable.value.shouldBeFalse()
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
                    root.isMutable shouldBeSameInstanceAs tempVec.root.isMutable
                    root.isMutable.value.shouldBeFalse()
                }

                """when the level is > 5 and the path is null,
                    it should create a new path then insert the tail""" {
                    val e = 99
                    val list = (1..2080).toList()
                    val tempVec = PersistentVector(*list.toTypedArray())
                    val tempTail = tempVec.tail
                    val tempIsMutable = tempVec.root.isMutable

                    val vec = tempVec.conj(e)
                    val root = vec.root
                    val subRoot = root.array[2] as Node<Int>
                    val mostRightLeaf = subRoot.array[0] as Node<Int>

                    vec.shift shouldBeExactly SHIFT * 2
                    mostRightLeaf.array shouldBeSameInstanceAs tempTail
                    vec.tail[0] shouldBe e
                    vec.count shouldBeExactly list.size + 1
                    root.isMutable.value.shouldBeFalse()
                    root.isMutable shouldBeSameInstanceAs tempIsMutable
                    subRoot.isMutable.value.shouldBeFalse()
                    subRoot.isMutable shouldBeSameInstanceAs tempIsMutable
                    mostRightLeaf.isMutable.value.shouldBeFalse()
                    mostRightLeaf.isMutable shouldBeSameInstanceAs tempIsMutable
                }

                "when root overflow, it should add 1 lvl by creating new root" {
                    val e = 99
                    val list = (1..1056).toList()
                    val tempVec = PersistentVector(*list.toTypedArray())
                    val tempIsMutable = tempVec.root.isMutable

                    val vec = tempVec.conj(e)
                    val root = vec.root
                    val subRoot1 = root.array[0] as Node<Int>
                    val subRoot2 = root.array[1] as Node<Int>
                    val mostRightLeaf = subRoot2.array[0] as Node<Int>

                    vec.count shouldBeExactly list.size + 1
                    vec.shift shouldBeExactly 10
                    vec.tail[0] as Int shouldBeExactly e
                    mostRightLeaf.array shouldBeSameInstanceAs tempVec.tail

                    root.isMutable.value.shouldBeFalse()
                    root.isMutable shouldBeSameInstanceAs tempIsMutable
                    subRoot1.isMutable shouldBeSameInstanceAs tempIsMutable
                    subRoot2.isMutable shouldBeSameInstanceAs tempIsMutable
                }
            }
        }

        "assocN(index, val)" - {

            "when index out of bounds, it should throw an exception"{
                val vec = v(1, 2, 3, 4)

                shouldThrowExactly<IndexOutOfBoundsException> {
                    vec.assocN(10, 15)
                }
            }

            "when index equals the count of the vector, it should conj" {
                val vec = v(1, 2, 3, 4)
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
                    val vec = v(1, 2, 3, 4)
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
                    val vec = v(*(1..50).toList().toTypedArray())
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
            v(1, 2, 3, 4).empty() shouldBeSameInstanceAs EmptyVector
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
            checkAll { list: List<Int> ->
                val vec = PersistentVector(*list.toTypedArray())

                vec.count shouldBeExactly list.size
            }
        }

        "toString()" {
            val vec = PersistentVector(1, 2, 3, 4)

            vec.toString() shouldBe "[1 2 3 4]"
        }

        "it's root should be immutable" {
            val vec = PersistentVector(1, 2, 3)

            vec.root.isMutable.value.shouldBeFalse()
        }

        "asTransient() should turn this vector into a transient vector" {
            val vec = PersistentVector(1, 2, 3, 4, 5, 6, 7, 8)

            val tVec: TransientVector<Int> = vec.asTransient()

            tVec.count shouldBeExactly vec.count
            tVec.shift shouldBeExactly vec.shift
            tVec.root.isMutable.value.shouldBeTrue()
            assertArraysAreEquiv(tVec.tail, vec.tail)
        }

        "seq()" - {
            "when called on an empty vector, it should return null" {
                val emptyVec = v<Int>()

                emptyVec.seq() shouldBeSameInstanceAs Empty
            }

            "when called on a filled vector, it should return a Seq instance" {
                val vec = v(1, 2, 3)

                val seq = vec.seq() as Seq<Int>
                val rest = seq.rest() as Seq<Int>

                seq.shouldNotBeNull()
                seq.count shouldBeExactly 3
                seq.first() shouldBeExactly 1
                seq.index shouldBeExactly 0

                rest.count shouldBeExactly 2
                rest.index shouldBeExactly 1
                rest.rest().first() shouldBeExactly 3
                rest.rest().rest() shouldBeSameInstanceAs Empty
            }
        }

        "hashCode()" - {
            "when called on EmptyVector, it should return 1" {
                EmptyVector.hashCode() shouldBeExactly 1
            }

            "when called on a populated vector it should calculate the hash" {
                val gen =
                    Arb.list(Arb.int().merge(Arb.int().map { null }), 1..20)
                checkAll(gen) { list: List<Int?> ->
                    val prime = 31
                    val expectedHash = list.fold(1) { hash, i ->
                        prime * hash + i.hashCode()
                    }
                    val vec = v(*list.toTypedArray())

                    vec.hashCode shouldBeExactly INIT_HASH_CODE
                    vec.hashCode() shouldBeExactly expectedHash
                    vec.hashCode shouldBeExactly expectedHash
                }

                EmptyVector.hashCode() shouldBeExactly 1
                EmptyVector.hashCode shouldBeExactly 1
            }
        }

        "equals(x)" {
            v(1, 2, 3, 4).equals(null).shouldBeFalse()

            (v(1) == v(1, 2, 3)).shouldBeFalse()

            (v(1, 2, 3) == v(1, 2, 3)).shouldBeTrue()

            val v = v(1, 2, 3)
            (v == v).shouldBeTrue()

            (v(1, 2, 3) == v(1, 2, 5)).shouldBeFalse()

            (v(v(1)) == v(v(1))).shouldBeTrue()

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
            val vec = v(1, 2, 3, 4)

            vec.hasheq shouldBeExactly 0

            val h = vec.fold(1) { hash: Int, i: Int ->
                (HASH_PRIME * hash) + hasheq(i)
            }
            val expectedHash = Murmur3.mixCollHash(h, vec.count)

            val hash = vec.hasheq()

            hash shouldBeExactly expectedHash
            vec.hasheq shouldBeExactly expectedHash
        }

        "valAt(key, default)" {
            val vec = v(1, 2, 3, 4)

            vec.valAt(4, default) shouldBe default
            vec.valAt(0, default) shouldBe 1
            vec.valAt(2, default) shouldBe 3
        }

        "valAt(key)" {
            val vec = v(1, 2, 3, 4)

            vec.valAt(4) shouldBe null
            vec.valAt(0) shouldBe 1
            vec.valAt(2) shouldBe 3
        }

        "containsKey(key)" {
            val vec = v(1, 2, 3, 4)

            vec.containsKey(0).shouldBeTrue()
            vec.containsKey(10).shouldBeFalse()
        }

        "entryAt(key)" {
            val vec = v(1, 2, 3, 4)

            val entry = vec.entryAt(2) as IMapEntry<Int, Int>

            entry.key shouldBeExactly 2
            entry.value shouldBeExactly 3

            vec.entryAt(6).shouldBeNull()
        }

        "assoc(key, value)" {
            val vec = v(1, 2, 3, 4)

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
            val vec = v(1, 2, 3, 4, 5, 6)

            val subvec = vec.subvec(start, end) as SubVector<Int>

            subvec.vec shouldBeSameInstanceAs vec
            subvec.start shouldBeExactly start
            subvec.end shouldBeExactly end
        }

        "compareTo(other)" - {
            "when this.count < other.count, it should return -1" {
                val vec1 = v(1, 2, 3)
                val vec2 = v(1, 2, 3, 4)

                vec1.compareTo(vec2) shouldBeExactly -1
            }

            "when this count > other count, it should return 1" {
                val vec1 = v(1, 2, 3, 4)
                val vec2 = v(1, 2, 3)

                vec1.compareTo(vec2) shouldBeExactly 1
            }

            "when this count == other count" - {

                "when all items are equal, it should return 0" {
                    v(1, 2, 3).compareTo(v(1, 2, 3)) shouldBeExactly 0
                    v<Number>(1L, 2).compareTo(v(1.0, 2)) shouldBeExactly 0
                    v<Any>(v(1, 2)).compareTo(v(v(1L, 2.0))) shouldBeExactly 0
                }

                "when this items < than other's, it should return -1" {
                    v(null, 2, 3).compareTo(v(1, 2, 3)) shouldBeExactly -1
                    v<Number>(1L, 2).compareTo(v(1.1, 2)) shouldBeExactly -1
                }

                "when this items > than other's, it should return 1" {
                    v<Int?>(1, 2, 3).compareTo(v(null, 2, 3)) shouldBeExactly 1
                    v<Number>(1.1, 2).compareTo(v(1L, 2)) shouldBeExactly 1
                    v<Any>(v(1.1, 2)).compareTo(v(v(1L, 2.0))) shouldBeExactly 1
                }
            }
        }

        "invoke(index) should return the associate value of the given index" {
            val vec = v(1, 2, 3, 4)

            vec(0) shouldBeExactly 1
            vec(1) shouldBeExactly 2
            vec(3) shouldBeExactly 4
            shouldThrowExactly<IndexOutOfBoundsException> { vec.nth(2000) }
        }

        "should be RandomAccess" {
            APersistentVector::class.shouldBeSubtypeOf<RandomAccess>()
        }

        "reverse()" - {
            "when the the vec is empty, it should return the empty seq" {
                val rseq: ISeq<Int> = v<Int>().reverse()

                rseq shouldBeSameInstanceAs Empty
            }

            "when vec is populated, it should return the reversed seq of it " {
                val vec = v(1, 2, 3, 4, 5)

                val rseq = vec.reverse() as RSeq<Int>

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
                    val v = v(*list.toTypedArray())

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
                    val v = v(*(1..33).toList().toTypedArray())

                    val vec = v.pop()

                    vec.count shouldBeExactly v.count - 1
                    vec.shift shouldBeExactly v.shift
                    vec.root shouldBeSameInstanceAs EmptyNode
                    vec.tail.size shouldBeExactly v.count - 1
                    vec(v.count - 2) shouldBeExactly v.nth(v.count - 2)
                }

                """when level = 5 & root contains more than 1 element after 
                   popping, it should set the rightmost leaf to null""" {
                    val v = v(*(1..65).toList().toTypedArray())

                    val vec = v.pop()
                    val root = vec.root

                    vec.shift shouldBeExactly 5
                    vec.count shouldBeExactly v.size - 1
                    vec.tail.size shouldBeExactly 32
                    vec.nth(0) shouldBeExactly 1
                    vec.tail[31] shouldBe 64

                    root.isMutable shouldBeSameInstanceAs v.root.isMutable
                    root.array[1].shouldBeNull()
                }

                """when level > 5 & root contains only 1 element after 
                   popping, it should decrease level and eliminate the empty
                   node""" {
                    val v = v(*(1..1057).toList().toTypedArray())

                    val vec = v.pop()
                    val root = vec.root

                    vec.shift shouldBeExactly 5
                    vec.count shouldBeExactly v.size - 1
                    vec.tail.size shouldBeExactly 32
                    root.isMutable shouldBeSameInstanceAs v.root.isMutable
                    root.array.fold(Unit) { _: Unit, element: Any? ->
                        element.shouldNotBeNull()
                    }
                }

                """when level > 10 & root contains only 1 element after 
                   popping, it should decrease level and eliminate the 
                   empty node""" {
                    val v = v(*(1..32801).toList().toTypedArray())

                    val vec = v.pop()
                    val root = vec.root

                    vec.shift shouldBeExactly 10
                    vec.count shouldBeExactly v.size - 1
                    vec.tail.size shouldBeExactly 32
                    root.isMutable shouldBeSameInstanceAs v.root.isMutable
                    root.array.fold(Unit) { _: Unit, element: Any? ->
                        element.shouldNotBeNull()
                    }
                }
            }
        }

        "+ operator should act like conj" {
            val vec = v(1, 2, 3, 4)

            val r = vec + 5

            r.count shouldBeExactly vec.count + 1
            r.nth(4) shouldBeExactly 5
        }

        "List implementation" - {
            "size()" {
                checkAll { l: List<Int> ->
                    val vec = v(*l.toTypedArray())

                    val list = vec as List<Int>

                    list.size shouldBeExactly l.size
                }
            }

            "get()" {
                val genA = Arb.list(Arb.int()).filter { it.isNotEmpty() }
                checkAll(genA) { l: List<Int> ->
                    val vec = v(*l.toTypedArray())

                    val list = vec as List<Int>

                    list[0] shouldBeExactly l[0]
                }
            }

            "isEmpty()" {
                v<Int>().isEmpty().shouldBeTrue()

                v(1, 2, 3, 4).isEmpty().shouldBeFalse()
            }

            "iterator()" {
                checkAll { list: List<Int> ->
                    val vec = v(*list.toTypedArray())

                    val iter = vec.iterator()

                    if (list.isEmpty()) iter.hasNext().shouldBeFalse()
                    else iter.hasNext().shouldBeTrue()

                    list.fold(Unit) { _: Unit, i: Int ->
                        iter.next() shouldBeExactly i
                    }

                    iter.hasNext().shouldBeFalse()
                    shouldThrowExactly<NoSuchElementException> { iter.next() }
                }
            }

            "indexOf(element)" {
                val vec = v(1L, 2.0, 3, 4)

                vec.indexOf(3) shouldBeExactly 2
                vec.indexOf(4L) shouldBeExactly 3
                vec.indexOf(1) shouldBeExactly 0
                vec.indexOf(6) shouldBeExactly -1
            }

            "lastIndexOf(element)" - {
                val vec = v(1, 1, 6, 6, 4, 5, 4)

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
                val vec = v(1, 2, 3, 4, 5)

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
                val vec = v(1, 2, 3, 4, 5)

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
                val vec = v(1, 2, 3, 4L)

                vec.contains(1).shouldBeTrue()
                vec.contains(1L).shouldBeTrue()

                vec.contains(15).shouldBeFalse()
            }

            "containsAll(elements)" {
                val vec = v(1, 2L, 3, 4, "a")

                vec.containsAll(v(1, 2, "a")).shouldBeTrue()

                vec.containsAll(v(1, 2, "b")).shouldBeFalse()
            }

            "subList(fromIndex, toIndex)" {
                val fromIndex = 1
                val toIndex = 5
                val vec = v(1, 2, 3, 4, 5, 6)

                val subvec = vec.subList(fromIndex, toIndex) as SubVector<Int>

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
            shouldThrowExactly<ArrayIndexOutOfBoundsException> {
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

            emptyVec.root.isMutable.value.shouldBeFalse()
        }
    }

    "TransientVector" - {

        @Suppress("UNCHECKED_CAST")
        "constructor" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val vRoot = v.root

            val tv = TransientVector(v)
            val tvRoot = tv.root

            tv.count shouldBeExactly v.count
            tv.shift shouldBeExactly v.shift
            tv.tail.size shouldBeExactly 32
            assertArraysAreEquiv(tv.tail, v.tail)
            tvRoot shouldNotBeSameInstanceAs vRoot
            tvRoot.array.size shouldBeExactly vRoot.array.size
            tvRoot.array shouldNotBeSameInstanceAs vRoot.array
            tvRoot.isMutable shouldNotBeSameInstanceAs vRoot.isMutable
            tvRoot.isMutable.value.shouldBeTrue()
            vRoot.array.fold(0) { index: Int, e: Any? ->

                if (e != null) {
                    val tvNode = tvRoot.array[index] as Node<Int>
                    val vNode = e as Node<Int>

                    tvNode shouldBe vNode
                    tvNode.isMutable shouldBeSameInstanceAs vRoot.isMutable
                }
                index + 1
            }
        }

        "invalidate() should set isMutable to false" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = TransientVector(v)
            val isMutable = tv.root.isMutable

            tv.invalidate()

            tv.root.isMutable shouldBeSameInstanceAs isMutable
            tv.root.isMutable.value.shouldBeFalse()
        }

        "assertMutable()" - {
            "when called on a mutable transient, it shouldn't throw" {
                val v = PersistentVector(*(1..57).toList().toTypedArray())
                val tv = TransientVector(v)

                shouldNotThrow<Exception> { tv.assertMutable() }
            }

            "when called on an invalidated transient, it should throw" {
                val v = PersistentVector(*(1..57).toList().toTypedArray())
                val tv = TransientVector(v)
                tv.invalidate()

                val e = shouldThrowExactly<IllegalStateException> {
                    tv.assertMutable()
                }

                e.message shouldBe "Transient used after persistent() call"
            }
        }

        "count" - {
            """when called on a mutable transient,
                        it should return the count of the transient vector""" {
                val v = PersistentVector(*(1..57).toList().toTypedArray())
                val tv = TransientVector(v)

                tv.root.isMutable.value.shouldBeTrue()
                tv.count shouldBeExactly v.count
            }

            """when called on a invalidated transient,
                                it should throw IllegalStateException""" {
                val v = PersistentVector(*(1..57).toList().toTypedArray())
                val tv = TransientVector(v)
                tv.invalidate()

                val e = shouldThrowExactly<IllegalStateException> { tv.count }

                e.message shouldBe "Transient used after persistent() call"
            }
        }

        "persistent()" - {
            "when called on a invalidated transient, when it should throw" {
                val v = PersistentVector(*(1..57).toList().toTypedArray())
                val tv = TransientVector(v)
                tv.invalidate()

                val e = shouldThrowExactly<IllegalStateException> {
                    tv.persistent()
                }

                e.message shouldBe "Transient used after persistent() call"
            }

            """when called on a mutable transient,
                it should return the PersistentVector of that transient,
                                                        and invalidate it""" {
                val v = PersistentVector(*(1..57).toList().toTypedArray())
                val tv = TransientVector(v)
                tv.tail = v.tail.copyOf(32)

                val vec = tv.persistent()
                val root = vec.root

                root.isMutable.value.shouldBeFalse()
                root shouldBeSameInstanceAs tv.root
                tv.root.isMutable.value.shouldBeFalse()
                vec.count shouldBeExactly v.count
                vec.shift shouldBeExactly v.shift
                vec.tail.size shouldBeExactly 25
                assertArraysAreEquiv(vec.tail, v.tail)
            }
        }

        "conj(e:E)" - {

            "when called on an invalidated transient, it should throw" {
                val tempTv = TransientVector(PersistentVector(1, 2, 3))
                tempTv.invalidate()

                val e = shouldThrowExactly<IllegalStateException> {
                    tempTv.conj(99)
                }

                e.message shouldBe "Transient used after persistent() call"
            }

            "when level is 5 and there is room in tail, it should add to it" {
                val i = 7
                val ints = Arb.int().filter { it != i }
                checkAll(Arb.list(ints, 0..31)) { list ->
                    val tempVec = PersistentVector(*list.toTypedArray())
                    val tempTv = TransientVector(tempVec)

                    val tv = tempTv.conj(i)
                    val tvTail = tv.tail
                    val tvRoot = tv.root

                    tv.shift shouldBeExactly SHIFT
                    tv.count shouldBeExactly list.size + 1
                    tvRoot.isMutable.value.shouldBeTrue()
                    tvRoot.array.size shouldBeExactly 32
                    tvTail.size shouldBeExactly 32
                    assertArraysAreEquiv(tvTail, tempVec.tail)

                    tvTail[list.size] as Int shouldBeExactly i
                }
            }

            "when tail overflow, it should push tail into the vec" - {
                @Suppress("UNCHECKED_CAST")
                "when level is 5, it should insert the tail in root node" {
                    val listGen = Arb.list(Arb.int(), (32..1024)).filter {
                        it.size % 32 == 0
                    }

                    checkAll(listGen, Arb.int()) { l: List<Int>, i: Int ->
                        val tempVec = PersistentVector(*l.toTypedArray())
                        val tempTv = TransientVector(tempVec)

                        val tv = tempTv.conj(i)
                        val tvTail = tv.tail
                        val tvRoot = tv.root

                        tv.shift shouldBeExactly SHIFT
                        var index = 31
                        var isMostRightLeafFound = false
                        while (index >= 0 && !isMostRightLeafFound) {
                            val node = tv.root.array[index]
                            if (node != null) {
                                val mostRightLeaf = node as Node<Int>
                                val array = mostRightLeaf.array

                                mostRightLeaf.isMutable shouldBeSameInstanceAs
                                    tempTv.root.isMutable
                                array shouldBe tempVec.tail
                                mostRightLeaf.isMutable shouldBeSameInstanceAs
                                    tempTv.root.isMutable
                                isMostRightLeafFound = true
                            }
                            index--
                        }

                        tvRoot.isMutable.value.shouldBeTrue()
                        isMostRightLeafFound.shouldBeTrue()
                        tv.count shouldBeExactly l.size + 1
                        tvTail.size shouldBeExactly 32
                        tvTail[0] shouldBe i
                    }
                }

                @Suppress("UNCHECKED_CAST")
                """when level is > 5, it should iterate through the
                                            levels then insert the tail""" {
                    val e = 99
                    val list = (1..1088).toList()
                    val tempVec = PersistentVector(*list.toTypedArray())
                    val root = tempVec.root
                    val tempTv = TransientVector(tempVec)

                    val tv = tempTv.conj(e)
                    val tvTail = tv.tail
                    val tvRoot = tv.root
                    val tvSubRoot = tvRoot.array[1] as Node<Int>
                    val firstLeft = tvSubRoot.array[0] as Node<Int>
                    val mostRightLeaf = tvSubRoot.array[1] as Node<Int>

                    tempTv.shift shouldBeExactly SHIFT * 2
                    tvTail[0] as Int shouldBeExactly e
                    assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)
                    tv.count shouldBeExactly list.size + 1

                    tvRoot.isMutable shouldNotBeSameInstanceAs root.isMutable
                    tvRoot.isMutable.value.shouldBeTrue()
                    tvSubRoot.isMutable shouldBeSameInstanceAs tvRoot.isMutable

                    firstLeft.isMutable shouldBeSameInstanceAs root.isMutable
                    mostRightLeaf.isMutable shouldBeSameInstanceAs
                        tvRoot.isMutable
                }

                @Suppress("UNCHECKED_CAST")
                """when level is > 5 and the subroot/path is null,
                        it should create a new path then insert the tail""" {
                    val e = 99
                    val list = (1..2080).toList()
                    val tempVec = PersistentVector(*list.toTypedArray())
                    val tempTv = TransientVector(tempVec)

                    val tv = tempTv.conj(e)
                    val tvTail = tv.tail
                    val tvRoot = tv.root
                    val tvSubRoot = tvRoot.array[2] as Node<Int>
                    val mostRightLeaf = tvSubRoot.array[0] as Node<Int>

                    tv.shift shouldBeExactly SHIFT * 2
                    assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)
                    tvTail[0] shouldBe e
                    tv.count shouldBeExactly list.size + 1
                    tvRoot.isMutable.value.shouldBeTrue()
                    tvSubRoot.isMutable shouldBeSameInstanceAs tvRoot.isMutable
                    mostRightLeaf.isMutable shouldBeSameInstanceAs
                        tvRoot.isMutable
                }

                @Suppress("UNCHECKED_CAST")
                "when root overflow, it should add 1 lvl by creating new root" {
                    val e = 99
                    val list = (1..1056).toList()
                    val tempVec = PersistentVector(*list.toTypedArray())
                    val tempTv = TransientVector(tempVec)

                    val tv = tempTv.conj(e)
                    val tvRoot = tv.root
                    val tvIsMutable = tvRoot.isMutable
                    val subRoot1 = tvRoot.array[0] as Node<Int>
                    val subRoot2 = tvRoot.array[1] as Node<Int>
                    val mostRightLeaf =
                        subRoot2.array[0] as Node<Int>

                    tv.count shouldBeExactly list.size + 1
                    tv.shift shouldBeExactly 10
                    tv.tail[0] as Int shouldBeExactly e
                    assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)

                    tvIsMutable shouldBeSameInstanceAs tempTv.root.isMutable
                    subRoot1.isMutable shouldBeSameInstanceAs tvIsMutable
                    subRoot2.isMutable shouldBeSameInstanceAs tvIsMutable
                    mostRightLeaf.isMutable shouldBeSameInstanceAs tvIsMutable
                }
            }
        }
    }

    "SubVector" - {
        "invoke()/ctor" - {
            "when start & end is out of bounds, it should throw exception" {
                val vec = v(1, 2, 3, 4, 5)

                val e1 = shouldThrowExactly<IndexOutOfBoundsException> {
                    SubVector(vec, 3, 2)
                }

                e1.message shouldBe "Make sure that the start < end: 3 < 2!"

                val e2 = shouldThrowExactly<IndexOutOfBoundsException> {
                    SubVector(vec, -1, 2)
                }

                e2.message shouldBe "Make sure that the start >= 0: -1 >= 0!"

                val e3 = shouldThrowExactly<IndexOutOfBoundsException> {
                    SubVector(vec, 1, 7)
                }

                e3.message shouldBe "Make sure that the end <= count: 7 <= 5!"
            }

            "when end == start, it should return the EmptyVector" {
                val vec = v(1, 2, 3, 4, 5)

                val empty = SubVector(vec, 2, 2)

                empty shouldBeSameInstanceAs EmptyVector
            }

            "when start & end are valid, it should return a SubVector" {
                val start = 1
                val end = 5
                val vec = v(1, 2, 3, 4, 5)

                val subvec = SubVector(vec, start, end) as SubVector<Int>

                subvec.vec shouldBeSameInstanceAs vec
                subvec.start shouldBeExactly start
                subvec.end shouldBeExactly end
            }
        }

        "count" {
            val start = 1
            val end = 5
            val vec = v(1, 2, 3, 4, 5)

            val subvec = SubVector(vec, start, end) as SubVector<Int>

            subvec.count shouldBeExactly end - start
        }

        "empty()" {
            val start = 1
            val end = 5
            val vec = v(1, 2, 3, 4, 5)

            SubVector(vec, start, end).empty() shouldBeSameInstanceAs
                EmptyVector
        }

        "nth(index)" {
            val vec = v(1, 2, 3, 4, 5)
            val subvec = SubVector(vec, 1, 5)

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
            val vec = v(1, 2, 3, 4, 5)
            val subvec = SubVector(vec, start, end)
            val e = 66

            val rsubvec = subvec.conj(e) as SubVector<Int>
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
                val vec = v(1, 2, 3, 4, 5)
                val subvec = SubVector(vec, 1, 5)

                shouldThrowExactly<IndexOutOfBoundsException> {
                    subvec.assocN(5, 75)
                }.message shouldBe "Index 5 is out of bounds."
            }

            "when index == count, it should append the value to the end" {
                val vec = v(1, 2, 3, 4, 5)
                val subvec = SubVector(vec, 1, 5)

                val r = subvec.assocN(4, 75) as SubVector<Int>

                r.nth(4) shouldBeExactly 75
            }

            "when 0 <= index < count, it should update the associate element" {
                val start = 1
                val end = 5
                val index = 3
                val value = 62
                val vec = v(1, 2, 3, 4, 5)
                val subvec = SubVector(vec, start, end)

                val r = subvec.assocN(index, value) as SubVector<Int>

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
                val vec = v(1, 2, 3, 4, 5)

                val subvec = SubVector(vec, start, end)

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
                val vec = v(1, 2, 3, 4, 5)
                val subvec = SubVector(vec, 1, 4)
                val subSubVec = SubVector(subvec, start, end)

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
                val vec = v(1, 2, 3, 4, 5)

                val subvec = SubVector(VectorMock(vec), start, end)

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
                val vec = v(1, 2, 3, 4, 5)
                val subvec = SubVector(vec, start, end) as SubVector<Int>

                val popped = subvec.pop() as SubVector<Int>

                popped.start shouldBeExactly start
                popped.vec shouldBeSameInstanceAs vec
                popped.end shouldBeExactly end - 1
            }

            "when the subvec count = 1, it should return the empty vector" {
                val start = 1
                val end = 2
                val vec = v(1, 2, 3, 4, 5)
                val subvec = SubVector(vec, start, end) as SubVector<Int>

                subvec.pop() shouldBeSameInstanceAs EmptyVector
            }
        }
    }

    "RSeq" - {
        "first" {
            checkAll(Arb.list(Arb.int(), 1..20)) { list: List<Int> ->
                val vec = v(*list.toTypedArray())
                val lastIndex = list.size - 1

                val rSeq = RSeq(vec, lastIndex)

                rSeq.count shouldBeExactly vec.count
                rSeq.first() shouldBeExactly vec[lastIndex]
            }
        }

        "rest()" - {
            "when the index is 0, it should return the empty seq" {
                val vec = v(1)
                val rseq = RSeq(vec, vec.size - 1)

                val rest = rseq.rest()

                rest shouldBeSameInstanceAs Empty
            }

            "when index > 0, it should return the rest of the reversed seq" {
                checkAll(Arb.list(Arb.int(), 2..20)) { list: List<Int> ->
                    val vec = v(*list.toTypedArray())
                    val lastIndex = list.size - 1

                    val rest = RSeq(vec, lastIndex).rest() as RSeq<Int>

                    rest.index shouldBeExactly lastIndex - 1
                    rest.count shouldBeExactly rest.index + 1
                    rest.first() shouldBeExactly vec[lastIndex - 1]

                    rest.rest().count shouldBeExactly rest.index
                }
            }
        }
    }

    "v() should return an empty persistent vector" {
        val empty = v<Int>()

        empty shouldBeSameInstanceAs EmptyVector
    }

    "v(args) should return an empty persistent vector" {
        val vec = v(1, 2, 3, 4)

        vec.count shouldBeExactly 4

        vec.nth(0) shouldBeExactly 1
        vec.nth(1) shouldBeExactly 2
        vec.nth(2) shouldBeExactly 3
        vec.nth(3) shouldBeExactly 4
    }

    "toPvector()" {
        listOf<Int>() shouldBe EmptyVector
        listOf(1, 2, "3", 4).toPvector() shouldBe v(1, 2, "3", 4)
    }

    "Serialization" - {
        "serialize" {
            val array = arrayOf(1, 2, 3, 4)
            val encoded = Json.encodeToString(array)
            val vec = v(*array)

            val encodeToString = Json.encodeToString(vec)

            encodeToString shouldBe encoded
        }

        "deserialize" {
            val array = arrayOf(1, 2, 3, 4)
            val str = Json.encodeToString(array)

            val vec = Json.decodeFromString<PersistentVector<Int>>(str)

            vec shouldBe PersistentList(*array)
        }

        "discriptor" {
            val element = serializer(Int::class.java)
            val serializer = PersistentVectorSerializer(element)

            serializer.descriptor shouldBeSameInstanceAs
                serializer.listSerializer.descriptor
        }
    }
}) {
    companion object {
        private fun assertArraysAreEquiv(a1: Array<Any?>, a2: Array<Any?>) {
            a2.fold(0) { index: Int, i: Any? ->
                val n = a1[index] as Int

                n shouldBeExactly i as Int

                index + 1
            }
        }
    }
}
