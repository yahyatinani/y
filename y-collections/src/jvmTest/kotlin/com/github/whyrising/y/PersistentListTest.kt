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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

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

        "equals()" {
            l(1, 2, 3).equals(null).shouldBeFalse()

            (l(1, 2, 3) == mapOf(1 to 2)).shouldBeFalse()

            (l(1, 2, 3) == listOf(1, 2, 3)).shouldBeTrue()

            (l(1, 2, 3) == listOf(1, 2, 3, 4)).shouldBeFalse()

            (l(User(1)) == listOf(User(2))).shouldBeFalse()

            (l(1, 2, 3) == l(1, 2, 8)).shouldBeFalse()

            (l(1, 2, 3) == Empty).shouldBeFalse()

            (l(1, 2, 3) == MockSeq(v(1, 2, 3))).shouldBeTrue()

            (l(1, 2, 3) == MockSeq(v(1, 2, 4))).shouldBeFalse()

            (l(1, 2, 3) == MockSeq(v(1, 2, 3, 4))).shouldBeFalse()
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

        "toString()" {
            PersistentList(1, 2, 3, 4, 5).toString() shouldBe "(1 2 3 4 5)"
        }

        "count" {
            checkAll { expect: List<Int> ->
                val list = PersistentList(*expect.toTypedArray())

                list.count shouldBeExactly expect.size
            }
        }

        "empty()" {
            val list = PersistentList(1, 2, 3)

            list.empty() shouldBe Empty
        }

        "equiv()" - {
            "should return false" {
                val list1 = PersistentList(1)
                val list2 = PersistentList(2)
                val list3 = PersistentList(2, null)
                val list4 = PersistentList(null, 2)
                val list5 = PersistentList(2, 3)
                val list6 = PersistentList(Any())
                val list7 = PersistentList(Any())

                (list1.equiv(null)).shouldBeFalse()
                (list1.equiv("list3")).shouldBeFalse()
                (list1.equiv(list2)).shouldBeFalse()
                (list1.equiv(list3)).shouldBeFalse()
                (list3.equiv(list5)).shouldBeFalse()
                (list4.equiv(list5)).shouldBeFalse()
                (list1.equiv(setOf(1))).shouldBeFalse()
                (list6.equiv(list7)).shouldBeFalse()
            }

            "should return true" {
                val list1 = PersistentList(1)
                val list2 = PersistentList(1)
                val list3 = PersistentList(1L)
                val list5 = PersistentList(PersistentList(1))
                val list6 = PersistentList(listOf(1L))

                list1.equiv(list2).shouldBeTrue()
                list1.equiv(listOf(1)).shouldBeTrue()
                list1.equiv(arrayListOf(1)).shouldBeTrue()
                list1.equiv(list3).shouldBeTrue()

                list5.equiv(list6).shouldBeTrue()
                list6.equiv(list5).shouldBeTrue()
            }
        }

        "conj() should return a persistent list of 1 element" {

            checkAll { l: List<Int>, i: Int ->
                val list = PersistentList(*l.toTypedArray())

                val r = list.conj(i) as PersistentList<Int>

                r.count shouldBeExactly l.size + 1
                r.first() shouldBeExactly i
            }
        }

        "seq() should return null" {
            val list = l(1, 2, 3)

            val seq = list.seq()

            seq shouldBeSameInstanceAs list
        }

        "implementation of List<E>" - {
            "size()" {
                checkAll { integers: List<Int> ->
                    val list = PersistentList(*integers.toTypedArray())

                    list.size shouldBeExactly integers.size
                }
            }

            "contains(element)" {
                val list = PersistentList(1, 2, 3, 4L, 1.4f)

                list.contains(4).shouldBeTrue()
                list.contains(10).shouldBeFalse()
            }

            "containsAll(Collection)"{
                val list = PersistentList(1, 2, 3, 4L, 1.4f)

                list.containsAll(listOf(3, 2, 4L)).shouldBeTrue()
                list.containsAll(listOf(3, 2, 0)).shouldBeFalse()
            }

            "indexOf(element)" {
                val list = PersistentList(1L, 2.0, 3, 4)

                list.indexOf(3) shouldBeExactly 2
                list.indexOf(4L) shouldBeExactly 3
                list.indexOf(1) shouldBeExactly 0
                list.indexOf(6) shouldBeExactly -1
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

                "when index is out of bounds, it should throw"{
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

    "Serialization" - {
        "serialize" {
            val l = listOf(1, 2, 3, 4)
            val encoded = Json.encodeToString(l)

            val list = PersistentList(*l.toTypedArray())

            val encodeToString = Json.encodeToString(list)

            encodeToString shouldBe encoded
        }

        "deserialize" {

            val l = listOf(1, 2, 3, 4)
            val str = Json.encodeToString(l)

            val list = Json.decodeFromString<PersistentList<Int>>(str)

            list shouldBe PersistentList(*l.toTypedArray())
        }
    }

    "l(args)" - {
        "l() without args should return an empty PersistentList" {
            l<Int>() shouldBe Empty
        }

        "l(args) with args should return a PersistentList" {
            l(1, 2, 3, 4) shouldBe PersistentList(1, 2, 3, 4)
        }
    }
})
