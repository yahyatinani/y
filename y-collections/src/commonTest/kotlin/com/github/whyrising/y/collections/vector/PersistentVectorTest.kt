package com.github.whyrising.y.collections.vector

import com.github.whyrising.y.collections.concretions.list.toPlist
import com.github.whyrising.y.collections.concretions.vector.BF
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.Node
import com.github.whyrising.y.collections.concretions.vector.SHIFT
import com.github.whyrising.y.collections.seq.ISeq
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import kotlin.test.Test

class PersistentVectorTest {
    @Test
    fun `invoke(seq) seq length is grater than 32`() {
        val l: ISeq<Int> = (1..39).toList().toPlist()

        val vec: PersistentVector<Int> = PersistentVector.invoke(l)

        vec.count shouldBeExactly l.count
        vec.shift shouldBeExactly SHIFT
        (vec.root.array[0] as Node<*>).array shouldContainAll (1..32).toList()
        vec.tail.size shouldBeExactly 7
        vec[32] shouldBeExactly 33
    }

    @Test
    fun `invoke(seq) seq length is 32`() {
        val l: ISeq<Int> = (1..32).toList().toPlist()

        val vec: PersistentVector<Int> = PersistentVector.invoke(l)

        vec.count shouldBeExactly BF
        vec.shift shouldBeExactly SHIFT
        vec.tail.size shouldBeExactly BF
        vec[31] shouldBeExactly 32
    }

    @Test
    fun `invoke(seq) seq length is less than 32`() {
        val l: ISeq<Int> = (1..15).toList().toPlist()

        val vec: PersistentVector<Int> = PersistentVector.invoke(l)

        vec.count shouldBeExactly 15
        vec.shift shouldBeExactly SHIFT
        vec.tail.size shouldBeExactly 15
        vec[14] shouldBeExactly 15
    }
}
