package com.github.whyrising.y

import com.github.whyrising.y.PersistentList.Cons
import com.github.whyrising.y.PersistentList.Empty
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.reflection.shouldBeSubtypeOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.merge
import io.kotest.property.checkAll

class PersistentListTest : FreeSpec({

    "PersistentList" - {
        "invoke() should return Empty list" {
            val emptyList = PersistentList<Int>()

            emptyList shouldBe Empty
        }

        "invoke(vararg) should return a PersistentList.Cons list" {
            checkAll { integers: List<Int> ->

                val list = PersistentList(*integers.toTypedArray())

                list.count shouldBeExactly integers.size
                list.zip(integers) { a, b ->
                    a.shouldNotBeNull()
                    a shouldBeExactly b
                }
            }
        }
    }

    "PersistentList.Empty" - {

        "implementation of List<E>" - {
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

        "should be a subtype of List<E>" {
            Empty::class.shouldBeSubtypeOf<PersistentList<*>>()
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

        "conj() should return a persistent list of 1 element" {
            val empty: PersistentList.AEmpty<Int> = Empty

            val list = empty.conj(1) as Cons<Int>

            list.first shouldBe 1
            list._rest shouldBe Empty
            list.count shouldBeExactly 1
        }

        "should implement a constant-time count" {
            Empty::class.shouldBeSubtypeOf<ConstantCount>()
        }

        "first property should throw NoSuchElementException" {

            val e = shouldThrowExactly<NoSuchElementException> { Empty.first() }

            e.message shouldBe "PersistentList is empty."
        }

        "rest property should return Empty" {
            val rest: ISeq<Int> = Empty.rest()

            rest shouldBe Empty
        }

        "cons(x) should return a seq of 1 element x" {
            checkAll { i: Int ->
                val seq: ISeq<Int> = (Empty as ISeq<Int>).cons(i)

                seq.first() shouldBe i
                (seq as Cons<Int>)._rest shouldBe Empty
                seq.count shouldBe 1
            }
        }

        // TODO: Serializable
        // TODO: Hash
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

        "cons(x) should add x as the first element to this seq" {
            val oldList = Cons(10, Empty)

            checkAll { i: Int ->

                val newList = oldList.cons(i)

                (newList as IPersistentList<*>).count shouldBeExactly 2
                newList.first() shouldBeExactly i
                newList.rest() shouldBeSameInstanceAs oldList
            }
        }

        "hashCode()" {
            val instsUnull = Arb.list(Arb.int().merge(Arb.int().map { null }))
            checkAll(instsUnull) { integers: List<Int?> ->
                val prime = 31

                val l = PersistentList(*integers.toTypedArray())

                l.hashCode() shouldBeExactly
                    integers.fold(Empty.hashCode()) { hashCode: Int, i: Int? ->
                        prime * hashCode + i.hashCode()
                    }
            }
        }

        "equals() should return true" {
            val list1 = PersistentList(1)
            val list2 = PersistentList(1)

            (list1 == list2).shouldBeTrue()
            (list1 == listOf(1)).shouldBeTrue()
        }

        "equals() should return false" {
            val list1 = PersistentList(1)
            val list2 = PersistentList(2)
            val list3 = PersistentList(2, null)

            (list1.equals(null)).shouldBeFalse()
            (list1.equals("list3")).shouldBeFalse()
            (list1 == list2).shouldBeFalse()
            (list2 == list3).shouldBeFalse()
        }

        "implementation of List<E>" - {
            "size()" {
                checkAll { integers: List<Int> ->
                    val list = PersistentList(*integers.toTypedArray())

                    list.size shouldBeExactly integers.size
                }
            }

            "isEmpty() should return false" {
                Cons(1, Empty).isEmpty().shouldBeFalse()
            }

            "iterator()" {
                val list: PersistentList<Int> = Cons(1, Empty)

                val seqIter = list.iterator() as SeqIterator

                seqIter.next shouldBeSameInstanceAs list
            }
        }

        "toString()" {
            PersistentList(1, 2, 3, 4, 5).toString() shouldBe "(1 2 3 4 5)"
        }
    }
})
