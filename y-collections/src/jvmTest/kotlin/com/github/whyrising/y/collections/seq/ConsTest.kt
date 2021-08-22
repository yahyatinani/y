package com.github.whyrising.y.collections.seq

import com.github.whyrising.y.collections.concretions.list.Cons
import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.core.l
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs

class ConsTest : FreeSpec({
    "ctor" {
        val restSeq = l(2, 3, 4) as ISeq<Int>

        Cons(1, restSeq)
    }

    "first()" {
        val restSeq = l(2, 3, 4) as ISeq<Int>

        val cons = Cons(1, restSeq)

        cons.first() shouldBeExactly 1
    }

    "rest" {
        val restSeq = l(2, 3, 4) as ISeq<Int>

        val cons = Cons(1, restSeq)

        cons.rest() shouldBeSameInstanceAs restSeq
    }

    "empty()" {
        val restSeq = l(2, 3, 4) as ISeq<Int>

        val empty = Cons(1, restSeq).empty()

        empty shouldBeSameInstanceAs Empty
    }

    "count" {
        val restSeq1 = l(2, 3, 4) as ISeq<Int>

        val cons1 = Cons(1, restSeq1)
        val cons2 = Cons(1, Cons(2, Empty))
        val cons3 = Cons(1, Cons(2, restSeq1))

        cons1.count shouldBeExactly 4
        cons2.count shouldBeExactly 2
        cons3.count shouldBeExactly 5
    }

    "cons(e)" {
        val e = 77
        val cons0 = Cons(1, l(2, 3, 4))

        val cons = cons0.cons(e)

        cons.count shouldBeExactly cons0.count + 1
        cons.first() shouldBeExactly e
        cons.rest() shouldBeSameInstanceAs cons0
    }

    "conj(e)" {
        val e = 77
        val cons0 = Cons(1, l(2, 3, 4))

        val cons = cons0.conj(e) as ISeq<Int>

        cons.count shouldBeExactly cons0.count + 1
        cons.first() shouldBeExactly e
        cons.rest() shouldBeSameInstanceAs cons0
    }
})
