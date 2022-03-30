package com.github.whyrising.y.collections.seq

import com.github.whyrising.y.collections.ArrayChunk
import com.github.whyrising.y.collections.concretions.list.ChunkedSeq
import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.concretions.list.SeqIterator
import com.github.whyrising.y.l
import com.github.whyrising.y.lazySeq
import com.github.whyrising.y.mocks.MockSeq
import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.utils.runAction
import com.github.whyrising.y.v
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LazySeqTest : FreeSpec({
    "ctor(fn)" {
        val chunk = ArrayChunk(arrayOf(1, 2, 3))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int> = { chunkedSeq }

        val lazySeq = LazySeq<Int>(f)

        lazySeq.f shouldBeSameInstanceAs f
        lazySeq.seq shouldBeSameInstanceAs Empty
        lazySeq.sVal.shouldBeNull()
    }

    "seqVal()" - {
        "when f != null & returns non null, it should set it to seqVal" {
            val chunk = ArrayChunk(arrayOf(1, 2, 3))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            val seqVal = lazySeq.seqVal()

            seqVal shouldBeSameInstanceAs f()
            lazySeq.sVal shouldBeSameInstanceAs f()
            lazySeq.f.shouldBeNull()
            lazySeq.seq shouldBeSameInstanceAs Empty
        }

        "when f != null & returns , it should return seq" {
            val chunk = ArrayChunk(arrayOf(1, 2, 3))
            val chunkedSeq = ChunkedSeq(chunk)
            val lazySeq = LazySeq<Int> { chunkedSeq }
            lazySeq.seq()

            var seqVal: Any? = null

            withContext(Dispatchers.Default) {
                runAction {
                    seqVal = lazySeq.seqVal()
                }
            }

            seqVal shouldBeSameInstanceAs chunkedSeq
            lazySeq.seq shouldBeSameInstanceAs chunkedSeq
            lazySeq.sVal.shouldBeNull()
            lazySeq.f.shouldBeNull()
        }
    }

    "seq" - {
        "seq()" {
            val chunk = ArrayChunk(arrayOf(1, 2, 3))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)
            var seq: ISeq<Int>? = null

            withContext(Dispatchers.Default) {
                runAction {
                    seq = lazySeq.seq()
                }
            }

            seq shouldBeSameInstanceAs chunkedSeq

            lazySeq.sVal.shouldBeNull()
            lazySeq.f.shouldBeNull()
            lazySeq.seq shouldBeSameInstanceAs chunkedSeq
        }

        """when f return LazySeq of LazySeq.., it should call seqVal() until
           it finds the inner seq""" {
            val chunk = ArrayChunk(arrayOf(1, 2, 3))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = {
                LazySeq { LazySeq<Int> { chunkedSeq } }
            }
            val lazySeq = LazySeq<Int>(f)
            var seq: ISeq<Int>? = null

            withContext(Dispatchers.Default) {
                runAction {
                    seq = lazySeq.seq()
                }
            }

            seq shouldBeSameInstanceAs chunkedSeq

            lazySeq.sVal.shouldBeNull()
            lazySeq.f.shouldBeNull()
            lazySeq.seq shouldBeSameInstanceAs chunkedSeq
        }
    }

    "empty() should return PersistentList.Empty" {
        val lazySeq = LazySeq<Int> { ChunkedSeq(ArrayChunk(arrayOf(1, 2, 3))) }

        lazySeq.empty() shouldBeSameInstanceAs Empty
    }

    "count" {
        val chunk = ArrayChunk(arrayOf(1, 2, 3))
        val chunkedSeq = ChunkedSeq(chunk)

        LazySeq<Int> { null }.count shouldBeExactly 0
        LazySeq<Int> { chunkedSeq }.count shouldBeExactly chunkedSeq.count
    }

    "cons(e)" {
        val e = 15
        val chunk = ArrayChunk(arrayOf(1, 2, 3))
        val chunkedSeq = ChunkedSeq(chunk)
        val lazySeq1 = LazySeq<Int> { null }
        val lazySeq2 = LazySeq<Int> { chunkedSeq }

        val lz1 = lazySeq1.cons(e)
        val lz2 = lazySeq2.cons(e)

        lz1.count shouldBeExactly lazySeq1.count + 1
        lz1.first() shouldBeExactly e

        lz2.count shouldBeExactly lazySeq2.count + 1
        lz2.first() shouldBeExactly e
    }

    "conj(e)" {
        val e = 15
        val chunk = ArrayChunk(arrayOf(1, 2, 3))
        val chunkedSeq = ChunkedSeq(chunk)
        val lazySeq1 = LazySeq<Int> { null }
        val lazySeq2 = LazySeq<Int> { chunkedSeq }

        val lz1 = lazySeq1.conj(e)
        val lz2 = lazySeq2.conj(e)

        lz1.count shouldBeExactly lazySeq1.count + 1
        lz1.first() shouldBeExactly e

        lz2.count shouldBeExactly lazySeq2.count + 1
        lz2.first() shouldBeExactly e
    }

    "first()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }
        val lazySeq = LazySeq<Int>(f)

        val first = lazySeq.first()

        first shouldBeExactly 45
        lazySeq.sVal.shouldBeNull()
        lazySeq.f.shouldBeNull()
        lazySeq.seq shouldBeSameInstanceAs chunkedSeq
    }

    "rest()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }
        val lazySeq = LazySeq<Int>(f)

        val rest = lazySeq.rest()

        rest.count shouldBeExactly 2
        rest.first() shouldBeExactly 89
        lazySeq.sVal.shouldBeNull()
        lazySeq.f.shouldBeNull()
        lazySeq.seq shouldBeSameInstanceAs chunkedSeq

        rest.rest().rest() shouldBeSameInstanceAs Empty
    }

    "equiv()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }
        val lazySeq = LazySeq<Int>(f)

        lazySeq.equiv(l(45, 89L, 36)).shouldBeTrue()
        LazySeq<Int> { null }.equiv(emptyList<Int>()).shouldBeTrue()
        LazySeq<Int> { null }.equiv(MockSeq<Int>(v())).shouldBeTrue()

        lazySeq.sVal.shouldBeNull()
        lazySeq.f.shouldBeNull()
        lazySeq.seq shouldBeSameInstanceAs chunkedSeq
    }

    "hashCode()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }
        val lazySeq = LazySeq<Int>(f)

        LazySeq<Int> { null }.hashCode() shouldBeExactly Empty.hashCode
        lazySeq.hashCode() shouldBeExactly chunkedSeq.hashCode()

        lazySeq.sVal.shouldBeNull()
        lazySeq.f.shouldBeNull()
        lazySeq.seq shouldBeSameInstanceAs chunkedSeq
    }

    "equals(other)" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }
        val lazySeq = LazySeq<Int>(f)

        lazySeq.equals(l(45, 89, 36)).shouldBeTrue()
        (LazySeq<Int> { null } == emptyList<Int>()).shouldBeTrue()
        LazySeq<Int> { null }.equals(MockSeq<Int>(v())).shouldBeTrue()

        lazySeq.sVal.shouldBeNull()
        lazySeq.f.shouldBeNull()
        lazySeq.seq shouldBeSameInstanceAs chunkedSeq
    }

    "hasheq()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }
        val lazySeq = LazySeq<Int>(f)

        lazySeq.hasheq() shouldBeExactly Murmur3.hashOrdered(lazySeq)
        lazySeq.sVal.shouldBeNull()
        lazySeq.f.shouldBeNull()
        lazySeq.seq shouldBeSameInstanceAs chunkedSeq
    }

    "isRealized()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))
        val chunkedSeq = ChunkedSeq(chunk)
        val f: () -> ISeq<Int>? = { chunkedSeq }

        val lazySeq = LazySeq<Int>(f)

        lazySeq.isRealized().shouldBeFalse()
        lazySeq.seq()
        lazySeq.isRealized().shouldBeTrue()
    }

    "toString()" {
        val chunk = ArrayChunk(arrayOf(45, 89, 36))

        LazySeq<Int> { ChunkedSeq(chunk) }.toString() shouldBe "(45 89 36)"
        LazySeq<Int> { null }.toString() shouldBe "()"
    }

    "List implementation" - {
        "iterator()" {
            val chunk = ArrayChunk(arrayOf(45, 89, 36))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            val seqIter = lazySeq.iterator() as SeqIterator<Int>

            seqIter.next() shouldBeExactly chunk.array[0]
        }

        "size" {
            val chunk = ArrayChunk(arrayOf(45, 89, 36))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            lazySeq.size shouldBeExactly lazySeq.count
        }

        "isEmpty()" {
            val chunk = ArrayChunk(arrayOf(45, 89, 36))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            lazySeq.isEmpty().shouldBeFalse()
            LazySeq<Int> { null }.isEmpty().shouldBeTrue()

            lazySeq.sVal.shouldBeNull()
            lazySeq.f.shouldBeNull()
            lazySeq.seq shouldBeSameInstanceAs chunkedSeq
        }

        "contains(element)" {
            val chunk = ArrayChunk(arrayOf(45, 89, 36))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Number>(f)

            lazySeq.contains(45L).shouldBeTrue()
            lazySeq.contains(89).shouldBeTrue()
            lazySeq.contains(100).shouldBeFalse()

            lazySeq.sVal.shouldBeNull()
            lazySeq.f.shouldBeNull()
            lazySeq.seq shouldBeSameInstanceAs chunkedSeq
        }

        "containsAll(coll)" {
            val chunk = ArrayChunk(arrayOf(45, 89, 36))
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Number>(f)

            lazySeq.containsAll(listOf(45, 89L)).shouldBeTrue()
            lazySeq.containsAll(listOf(45, 70)).shouldBeFalse()

            lazySeq.sVal.shouldBeNull()
            lazySeq.f.shouldBeNull()
            lazySeq.seq shouldBeSameInstanceAs chunkedSeq
        }

        "get(index)" {
            val array = arrayOf(45, 89, 36)
            val chunk = ArrayChunk(array)
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            lazySeq[0] shouldBeExactly array[0]
            lazySeq[1] shouldBeExactly array[1]
            lazySeq[2] shouldBeExactly array[2]
        }

        "indexOf(element)" {
            val array = arrayOf(45, 89, 36)
            val chunk = ArrayChunk(array)
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Number>(f)

            lazySeq.indexOf(45L) shouldBeExactly 0
            lazySeq.indexOf(89L) shouldBeExactly 1
            lazySeq.indexOf(36) shouldBeExactly 2
            lazySeq.indexOf(100) shouldBeExactly -1
        }

        "lastIndexOf(element)" {
            val array = arrayOf(45, 89, 36, 36)
            val chunk = ArrayChunk(array)
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            lazySeq.lastIndexOf(36) shouldBeExactly 3
            lazySeq.lastIndexOf(45) shouldBeExactly 0
        }

        "listIterator()" {
            val array = arrayOf(45, 89, 36, 36)
            val chunk = ArrayChunk(array)
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            val listIter = lazySeq.listIterator()

            listIter.next() shouldBeExactly 45
            listIter.next() shouldBeExactly 89
        }

        "listIterator(index)" {
            val array = arrayOf(45, 89, 36, 72)
            val chunk = ArrayChunk(array)
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            val listIter = lazySeq.listIterator(1)

            listIter.next() shouldBeExactly 89
            listIter.next() shouldBeExactly 36
        }

        "subList(fromIndex: Int, toIndex: Int)" {
            val array = arrayOf(45, 89, 36, 72)
            val chunk = ArrayChunk(array)
            val chunkedSeq = ChunkedSeq(chunk)
            val f: () -> ISeq<Int>? = { chunkedSeq }
            val lazySeq = LazySeq<Int>(f)

            val subList = lazySeq.subList(1, 3)

            subList.size shouldBeExactly 2
            subList[0] shouldBeExactly 89
            subList[1] shouldBeExactly 36
        }
    }

    "empty lazySeq()" {
        val seq = lazySeq<Int>()

        seq.count shouldBeExactly 0
        seq.toString() shouldBe "()"
    }

    "lazySeq(lazySeq)" {
        val seq = lazySeq<Int> { lazySeq<Int> { l(1) } }

        seq.toString() shouldBe "(1)"
        seq.count shouldBeExactly 1
    }
})
