package com.github.whyrising.y.vector

import com.github.whyrising.y.concretions.vector.PersistentVector
import com.github.whyrising.y.concretions.vector.v
import com.github.whyrising.y.map.arraymap.runAction
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
@ExperimentalStdlibApi
class TransientVectorTest : FreeSpec({
    @Suppress("UNCHECKED_CAST")
    "constructor" {
        val v = PersistentVector(*(1..57).toList().toTypedArray())
        val vRoot = v.root

        val tv = PersistentVector.TransientVector(v)
        val tvRoot = tv.root

        tv.count shouldBeExactly v.count
        tv.shift shouldBeExactly v.shift
        tv.tail.size shouldBeExactly 32
        PersistentVectorTest.assertArraysAreEquiv(tv.tail, v.tail)
        tvRoot shouldNotBeSameInstanceAs vRoot
        tvRoot.array.size shouldBeExactly vRoot.array.size
        tvRoot.array shouldNotBeSameInstanceAs vRoot.array
        tvRoot.isMutable shouldNotBeSameInstanceAs vRoot.isMutable
        tvRoot.isMutable.value.shouldBeTrue()
        vRoot.array.fold(0) { index: Int, e: Any? ->

            if (e != null) {
                val tvNode = tvRoot.array[index] as PersistentVector.Node<Int>
                val vNode = e as PersistentVector.Node<Int>

                tvNode shouldBe vNode
                tvNode.isMutable shouldBeSameInstanceAs vRoot.isMutable
            }
            index + 1
        }
    }

    "invalidate() should set isMutable to false" {
        val v = PersistentVector(*(1..57).toList().toTypedArray())
        val tv = PersistentVector.TransientVector(v)
        val isMutable = tv.root.isMutable

        tv.invalidate()

        tv.root.isMutable shouldBeSameInstanceAs isMutable
        tv.root.isMutable.value.shouldBeFalse()
    }

    "assertMutable()" - {
        "when called on a mutable transient, it shouldn't throw" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)

            shouldNotThrow<Exception> { tv.assertMutable() }
        }

        "when called on an invalidated transient, it should throw" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)
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
            val tv = PersistentVector.TransientVector(v)

            tv.root.isMutable.value.shouldBeTrue()
            tv.count shouldBeExactly v.count
        }

        """when called on a invalidated transient,
                                it should throw IllegalStateException""" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)
            tv.invalidate()

            val e = shouldThrowExactly<IllegalStateException> { tv.count }

            e.message shouldBe "Transient used after persistent() call"
        }
    }

    "persistent()" - {
        "when called on a invalidated transient, when it should throw" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)
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
            val tv = PersistentVector.TransientVector(v)
            tv.tail = v.tail.copyOf(32)

            val vec = tv.persistent()
            val root = vec.root

            root.isMutable.value.shouldBeFalse()
            root shouldBeSameInstanceAs tv.root
            tv.root.isMutable.value.shouldBeFalse()
            vec.count shouldBeExactly v.count
            vec.shift shouldBeExactly v.shift
            vec.tail.size shouldBeExactly 25
            PersistentVectorTest.assertArraysAreEquiv(vec.tail, v.tail)
        }
    }

    "conj(e:E)" - {

        "when called on an invalidated transient, it should throw" {
            val tempTv = PersistentVector.TransientVector(PersistentVector(1, 2, 3))
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
                val tempTv = PersistentVector.TransientVector(tempVec)

                val tv = tempTv.conj(i)
                val tvTail = tv.tail
                val tvRoot = tv.root

                tv.shift shouldBeExactly SHIFT
                tv.count shouldBeExactly list.size + 1
                tvRoot.isMutable.value.shouldBeTrue()
                tvRoot.array.size shouldBeExactly 32
                tvTail.size shouldBeExactly 32
                PersistentVectorTest.assertArraysAreEquiv(tvTail, tempVec.tail)

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
                    val tempTv = PersistentVector.TransientVector(tempVec)

                    val tv = tempTv.conj(i)
                    val tvTail = tv.tail
                    val tvRoot = tv.root

                    tv.shift shouldBeExactly SHIFT
                    var index = 31
                    var isMostRightLeafFound = false
                    while (index >= 0 && !isMostRightLeafFound) {
                        val node = tv.root.array[index]
                        if (node != null) {
                            val mostRightLeaf = node as PersistentVector.Node<Int>
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
                val tempTv = PersistentVector.TransientVector(tempVec)

                val tv = tempTv.conj(e)
                val tvTail = tv.tail
                val tvRoot = tv.root
                val tvSubRoot = tvRoot.array[1] as PersistentVector.Node<Int>
                val firstLeft = tvSubRoot.array[0] as PersistentVector.Node<Int>
                val mostRightLeaf = tvSubRoot.array[1] as PersistentVector.Node<Int>

                tempTv.shift shouldBeExactly SHIFT * 2
                tvTail[0] as Int shouldBeExactly e
                PersistentVectorTest.assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)
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
                val tempTv = PersistentVector.TransientVector(tempVec)

                val tv = tempTv.conj(e)
                val tvTail = tv.tail
                val tvRoot = tv.root
                val tvSubRoot = tvRoot.array[2] as PersistentVector.Node<Int>
                val mostRightLeaf = tvSubRoot.array[0] as PersistentVector.Node<Int>

                tv.shift shouldBeExactly SHIFT * 2
                PersistentVectorTest.assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)
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
                val tempTv = PersistentVector.TransientVector(tempVec)

                val tv = tempTv.conj(e)
                val tvRoot = tv.root
                val tvIsMutable = tvRoot.isMutable
                val subRoot1 = tvRoot.array[0] as PersistentVector.Node<Int>
                val subRoot2 = tvRoot.array[1] as PersistentVector.Node<Int>
                val mostRightLeaf =
                    subRoot2.array[0] as PersistentVector.Node<Int>

                tv.count shouldBeExactly list.size + 1
                tv.shift shouldBeExactly 10
                tv.tail[0] as Int shouldBeExactly e
                PersistentVectorTest.assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)

                tvIsMutable shouldBeSameInstanceAs tempTv.root.isMutable
                subRoot1.isMutable shouldBeSameInstanceAs tvIsMutable
                subRoot2.isMutable shouldBeSameInstanceAs tvIsMutable
                mostRightLeaf.isMutable shouldBeSameInstanceAs tvIsMutable
            }
        }
    }

    "concurrency" {
        val v = v<Int>()
        val transientVec = PersistentVector.TransientVector(v)

        withContext(Dispatchers.Default) {
            runAction(100, 1000) {
                transientVec.conj(1)
            }
        }

        transientVec.count shouldBeExactly 100000
    }
})
