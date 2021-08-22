package com.github.whyrising.y.collections.vector

import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.vector.PersistentVectorJvmTest.Companion.assertArraysAreEquiv
import com.github.whyrising.y.utils.runAction
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.framework.concurrency.continually
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration

@ExperimentalKotest
@ExperimentalSerializationApi
@ExperimentalStdlibApi
class TransientVectorTest : FreeSpec({
    @Suppress("UNCHECKED_CAST")
    "constructor" {
        val v = PersistentVector(*(1..57).toList().toTypedArray())
        val root = v.root

        val transientVector = PersistentVector.TransientVector(v)
        val tvRoot = transientVector.root

        transientVector.count shouldBeExactly v.count
        transientVector.shift shouldBeExactly v.shift
        transientVector.tail.size shouldBeExactly 32
        assertArraysAreEquiv(transientVector.tail, v.tail)
        tvRoot shouldNotBeSameInstanceAs root
        tvRoot.array.size shouldBeExactly root.array.size
        tvRoot.array shouldNotBeSameInstanceAs root.array
        tvRoot.edit.shouldNotBeNull()
        root.edit.value.shouldBeNull()
        val vNode = root.array[0] as PersistentVector.Node<Int>
        val tvNode = tvRoot.array[0] as PersistentVector.Node<Int>

        tvNode shouldBeSameInstanceAs vNode

        tvNode.edit shouldBeSameInstanceAs vNode.edit
        tvNode.edit.value.shouldBeNull()
    }

    "invalidate() should set isMutable to false" {
        val v = PersistentVector(*(1..57).toList().toTypedArray())
        val tv = PersistentVector.TransientVector(v)
        val isMutable = tv.root.edit

        tv.invalidate()

        tv.root.edit shouldBeSameInstanceAs isMutable
        tv.root.edit.value.shouldBeNull()
    }

    "assertMutable()" - {
        "when called on a mutable transient, it shouldn't throw" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)

            shouldNotThrow<Exception> { tv.ensureEditable() }
        }

        "when called on an invalidated transient, it should throw" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)
            tv.invalidate()

            val e = shouldThrowExactly<IllegalStateException> {
                tv.ensureEditable()
            }

            e.message shouldBe "Transient used after persistent() call"
        }
    }

    "count" - {
        """when called on a mutable transient,
               it should return the count of the transient vector""" {
            val v = PersistentVector(*(1..57).toList().toTypedArray())
            val tv = PersistentVector.TransientVector(v)

            tv.root.edit.shouldNotBeNull()
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

            root.edit.value.shouldBeNull()
            root shouldBeSameInstanceAs tv.root
            tv.root.edit.value.shouldBeNull()
            vec.count shouldBeExactly v.count
            vec.shift shouldBeExactly v.shift
            vec.tail.size shouldBeExactly 25
            assertArraysAreEquiv(vec.tail, v.tail)
        }
    }

    "conj(e:E)" - {

        "when called on an invalidated transient, it should throw" {
            val tempTv =
                PersistentVector.TransientVector(PersistentVector(1, 2, 3))
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
                tvRoot.edit.shouldNotBeNull()
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
                            val mostRightLeaf =
                                node as PersistentVector.Node<Int>
                            val array = mostRightLeaf.array

                            mostRightLeaf.edit shouldBeSameInstanceAs
                                tempTv.root.edit
                            array shouldBe tempVec.tail
                            mostRightLeaf.edit shouldBeSameInstanceAs
                                tempTv.root.edit
                            isMostRightLeafFound = true
                        }
                        index--
                    }

                    tvRoot.edit.shouldNotBeNull()
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
                val mostRightLeaf =
                    tvSubRoot.array[1] as PersistentVector.Node<Int>

                tempTv.shift shouldBeExactly SHIFT * 2
                tvTail[0] as Int shouldBeExactly e
                assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)
                tv.count shouldBeExactly list.size + 1

                tvRoot.edit shouldNotBeSameInstanceAs root.edit
                tvRoot.edit.shouldNotBeNull()
                tvSubRoot.edit shouldBeSameInstanceAs tvRoot.edit

                firstLeft.edit shouldBeSameInstanceAs root.edit
                mostRightLeaf.edit shouldBeSameInstanceAs
                    tvRoot.edit
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
                val mostRightLeaf =
                    tvSubRoot.array[0] as PersistentVector.Node<Int>

                tv.shift shouldBeExactly SHIFT * 2
                assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)
                tvTail[0] shouldBe e
                tv.count shouldBeExactly list.size + 1
                tvRoot.edit.shouldNotBeNull()
                tvSubRoot.edit shouldBeSameInstanceAs tvRoot.edit
                mostRightLeaf.edit shouldBeSameInstanceAs
                    tvRoot.edit
            }

            @Suppress("UNCHECKED_CAST")
            "when root overflow, it should add 1 lvl by creating new root" {
                val e = 99
                val list = (1..1056).toList()
                val tempVec = PersistentVector(*list.toTypedArray())
                val tempTv = PersistentVector.TransientVector(tempVec)

                val tv = tempTv.conj(e)
                val tvRoot = tv.root
                val tvIsMutable = tvRoot.edit
                val subRoot1 = tvRoot.array[0] as PersistentVector.Node<Int>
                val subRoot2 = tvRoot.array[1] as PersistentVector.Node<Int>
                val mostRightLeaf =
                    subRoot2.array[0] as PersistentVector.Node<Int>

                tv.count shouldBeExactly list.size + 1
                tv.shift shouldBeExactly 10
                tv.tail[0] as Int shouldBeExactly e
                assertArraysAreEquiv(mostRightLeaf.array, tempVec.tail)

                tvIsMutable shouldBeSameInstanceAs tempTv.root.edit
                subRoot1.edit shouldBeSameInstanceAs tvIsMutable
                subRoot2.edit shouldBeSameInstanceAs tvIsMutable
                mostRightLeaf.edit shouldBeSameInstanceAs tvIsMutable
            }
        }
    }

    "concurrency" {
        val i = 45
        val v = PersistentVector<Int>()

        continually(Duration.seconds(10)) {
            val transientVec = PersistentVector.TransientVector(v)

            withContext(Dispatchers.Default) {
                runAction(100, 10) {
                    transientVec.conj(i)
                }
            }

            transientVec.count shouldBeExactly 1000

            val vec = transientVec.persistent()

            vec shouldContain 45
        }
    }
})
