package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.PersistentList
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class StringSeqTest {
    @Test
    fun `ctor()`() {
        val stringSeq = StringSeq("abcd", 0)

        stringSeq.s shouldBe "abcd"
        stringSeq.i shouldBe 0
    }

    @Test
    fun `first()`() {
        StringSeq("") shouldBeSameInstanceAs PersistentList.Empty
        StringSeq("abcd", 0).first() shouldBe 'a'
        StringSeq("bcda", 0).first() shouldBe 'b'
        StringSeq("Tcda", 0).first() shouldBe 'T'
        StringSeq("Tcda", 1).first() shouldBe 'c'
        StringSeq("Tcda", 3).first() shouldBe 'a'
    }

    @Test
    fun `rest()`() {
        val stringSeq = StringSeq("abcd", 0)

        val rest1 = stringSeq.rest() as StringSeq
        val rest2 = rest1.rest() as StringSeq
        val rest3 = rest2.rest() as StringSeq
        val rest4 = rest3.rest()

        rest1.s shouldBe "abcd"
        rest1.i shouldBeExactly stringSeq.i + 1

        rest2.s shouldBe "abcd"
        rest2.i shouldBeExactly stringSeq.i + 2

        rest3.s shouldBe "abcd"
        rest3.i shouldBeExactly stringSeq.i + 3

        rest4 shouldBeSameInstanceAs PersistentList.Empty
    }

    @Test
    fun count() {
        val stringSeq = StringSeq("abcd", 0)

        val rest1 = stringSeq.rest() as StringSeq
        val rest2 = rest1.rest() as StringSeq
        val rest3 = rest2.rest() as StringSeq
        val rest4 = rest3.rest()

        stringSeq.count shouldBeExactly 4
        rest1.count shouldBeExactly 3
        rest2.count shouldBeExactly 2
        rest3.count shouldBeExactly 1
        rest4.count shouldBeExactly 0
    }

    @Test
    fun index() {
        val stringSeq = StringSeq("abcd", 0)
        val rest1 = stringSeq.rest() as StringSeq
        val rest2 = rest1.rest() as StringSeq
        val rest3 = rest2.rest() as StringSeq

        stringSeq.index shouldBeExactly 0
        rest1.index shouldBeExactly 1
        rest2.index shouldBeExactly 2
        rest3.index shouldBeExactly 3
    }
}