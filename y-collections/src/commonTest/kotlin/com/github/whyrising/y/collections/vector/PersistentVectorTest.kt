package com.github.whyrising.y.collections.vector

import com.github.whyrising.y.collections.concretions.vector.BF
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.Node
import com.github.whyrising.y.collections.concretions.vector.SHIFT
import com.github.whyrising.y.collections.core.l
import com.github.whyrising.y.collections.core.toPlist
import com.github.whyrising.y.collections.core.v
import com.github.whyrising.y.collections.core.vec
import com.github.whyrising.y.collections.seq.ISeq
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
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

    @Test
    fun `vec()`() {
        vec<Any>(null) shouldBeSameInstanceAs PersistentVector.EmptyVector

        vec(listOf(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

        vec(arrayListOf(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

        vec(l(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

        vec<Any>(arrayOf(1, 2, "3", 4)) shouldBe v(1, 2, "3", 4)

        vec<Int>(intArrayOf(1, 2, 4)) shouldBe v(1, 2, 4)

        vec<Short>(shortArrayOf(1, 2)) shouldBe v(1.toShort(), 2.toShort())

        vec<Byte>(byteArrayOf(1, 2)) shouldBe v(1.toByte(), 2.toByte())

        vec<Boolean>(booleanArrayOf(true, false)) shouldBe v(true, false)

        vec<Char>(charArrayOf('a', 'b')) shouldBe v('a', 'b')

        vec<Float>(floatArrayOf(1F, 2F)) shouldBe v(1F, 2F)

        vec<Double>(doubleArrayOf(1.0, 2.0)) shouldBe v(1.0, 2.0)

        vec<Long>(longArrayOf(1L, 2L)) shouldBe v(1L, 2L)
    }
}
