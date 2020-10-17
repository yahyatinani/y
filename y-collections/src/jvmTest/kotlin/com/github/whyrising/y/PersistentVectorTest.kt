package com.github.whyrising.y

import com.github.whyrising.y.PersistentVector.EmptyVector
import com.github.whyrising.y.PersistentVector.Node
import com.github.whyrising.y.PersistentVector.Node.EmptyNode
import com.github.whyrising.y.PersistentVector.TransientVector
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
import kotlinx.atomicfu.atomic

const val SHIFT = 5

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

        "conj(e)" - {
            val i = 7
            "when the vec size is < 32, it should add e to the tail" {
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
                    val mostRightLeaf = ((root.array[1] as Node<Int>).array[1]
                        as Node<Int>).array

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

                "root overflow" {
                    val e = 99
                    val list = (1..1056).toList()
                    val tempVec = PersistentVector(*list.toTypedArray())

                    val vec = tempVec.conj(e)
                    val root = vec.root
                    val mostRightLeaf =
                        (root.array[1] as Node<Int>).array[0] as Node<Int>


                    vec.count shouldBeExactly list.size + 1
                    vec.shift shouldBeExactly 10
                    vec.tail[0] as Int shouldBeExactly e
                    root.array[0].shouldNotBeNull()
                    root.array[1].shouldNotBeNull()
                    root.isMutable shouldBeSameInstanceAs tempVec.root.isMutable
                    root.isMutable.value.shouldBeFalse()
                    mostRightLeaf.array shouldBeSameInstanceAs tempVec.tail
                    mostRightLeaf.array[0] shouldBe 1025
                    mostRightLeaf.array[31] shouldBe 1056
                }
            }
        }

        "length()" {
            checkAll { list: List<Int> ->
                val vec = PersistentVector(*list.toTypedArray())

                vec.length() shouldBeExactly list.size
            }
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

        "it's root should be immutable" {
            val emptyVec = PersistentVector<Int>()

            emptyVec.root.isMutable.value.shouldBeFalse()
        }
    }

    "TransientVector" - {
        "constructor" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val vRoot = v.root

            val tv = TransientVector(v)
            val tvRoot = tv.root.value

            tv.count.value shouldBeExactly v.count
            tv.shift.value shouldBeExactly v.shift
            tv.tail.value shouldBeSameInstanceAs v.tail
            tvRoot shouldNotBeSameInstanceAs vRoot
            tvRoot.array shouldNotBeSameInstanceAs vRoot.array
            tvRoot.array.size shouldBeExactly vRoot.array.size
            tvRoot.isMutable shouldNotBeSameInstanceAs vRoot.isMutable
            tvRoot.isMutable.value.shouldBeTrue()
            vRoot.array.fold(0) { index: Int, e: Any? ->
                e shouldBe tvRoot.array[index]
                index + 1
            }
        }
    }
})
