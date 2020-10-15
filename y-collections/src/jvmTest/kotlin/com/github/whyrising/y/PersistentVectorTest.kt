package com.github.whyrising.y

import com.github.whyrising.y.PersistentVector.EmptyPersistentVector
import com.github.whyrising.y.PersistentVector.Node
import com.github.whyrising.y.PersistentVector.Node.EmptyNode
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll

const val SHIFT = 5

class PersistentVectorTest : FreeSpec({
    "Node" - {
        @Suppress("UNCHECKED_CAST")
        "Node does have an array of nodes" {
            val array = arrayOfNulls<Int>(33)

            val node = PersistentVector.Node<Int>(array as Array<Any?>)

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
        "invoke() should return the EmptyPersistentVector" {
            PersistentVector<Int>() shouldBe EmptyPersistentVector
        }

        "invoke(args)" - {
            "when args count <= 32, it should add to the tail" {
                checkAll(Arb.list(Arb.int(), 0..32)) { list: List<Int> ->
                    val vec = PersistentVector(*list.toTypedArray())
                    val tail = vec.tail

                    if (vec.count == 0)
                        vec shouldBeSameInstanceAs EmptyPersistentVector
                    vec.shift shouldBeExactly SHIFT
                    vec.count shouldBeExactly list.size
                    vec.root shouldBeSameInstanceAs EmptyNode
                    tail.size shouldBeExactly list.size
                    tail shouldContainAll list
                }
            }
        }

        "conj(e)" - {
            val i = 7
            "when the vec size is < 32, it should add e to the tail" {
                val ints = Arb.int().filter { it != i }
                checkAll(Arb.list(ints, 0..31)) { list ->
                    val vec = PersistentVector(*list.toTypedArray()).conj(i)
                    val tail = vec.tail

                    vec.shift shouldBeExactly SHIFT
                    vec.count shouldBeExactly list.size + 1
                    vec.root shouldBeSameInstanceAs EmptyNode
                    tail.size shouldBeExactly list.size + 1
                    tail[list.size] as Int shouldBeExactly i
                }
            }

            @Suppress("UNCHECKED_CAST")
            "when the tail is full, it should push tail into the vec" - {
                "when size is 32 and there is enough room in the root" {
                    val e = 99
                    val list = (1..32).toList()
                    val pv = PersistentVector(*list.toTypedArray())

                    val vec = pv.conj(e)
                    val tail = vec.tail
                    val root = vec.root
                    val firstNodeElements = (root.array[0] as Node<Int>).array

                    vec.shift shouldBeExactly SHIFT
                    vec.count shouldBeExactly 33
                    root.array.size shouldBeExactly 32
                    firstNodeElements shouldBeSameInstanceAs pv.tail
                    for ((index, n) in list.withIndex())
                        firstNodeElements[index] as Int shouldBeExactly n
                    tail.size shouldBeExactly 1
                    tail[0] as Int shouldBeExactly e
                }

                "when size is 64 and there is enough room in the root" {
                    val e = 99
                    val list1 = (1..32).toList()
                    val list2 = (33..64).toList()
                    val chunk1 = PersistentVector(*list1.toTypedArray())
                    val chunk2 = list2.fold(chunk1) { v, i -> v.conj(i) }

                    val vec = chunk2.conj(e)
                    val root = vec.root
                    val tail = vec.tail
                    val firstNodeElements = (root.array[0] as Node<Int>).array
                    val secondNodeElements = (root.array[1] as Node<Int>).array

                    firstNodeElements shouldBeSameInstanceAs chunk1.tail
                    secondNodeElements shouldBeSameInstanceAs chunk2.tail
                    vec.count shouldBeExactly 65
                    tail.size shouldBeExactly 1
                    tail[0] as Int shouldBeExactly e
                }

                "root overflow" {
                    val e = 99
                    val list = (1..1056).toList()

                    val vec = list.fold(PersistentVector<Int>()) { v, i ->
                        v.conj(i)
                    }.conj(e)
                    val leaf33 = (vec.root.array[1] as Node<Int>).array[0]
                        as Node<Int>

                    vec.count shouldBeExactly list.size + 1
                    vec.shift shouldBeExactly 10
                    vec.tail[0] as Int shouldBeExactly e
                    vec.root.array[0].shouldNotBeNull()
                    leaf33.array[0] shouldBe 1025
                    leaf33.array[31] shouldBe 1056
                }
            }
        }
    }

    "EmptyPersistentVector" - {
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
    }
})
