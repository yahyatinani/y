package com.github.whyrising.y.collections.map

import com.github.whyrising.y.collections.concretions.map.HASHTABLE_THRESHOLD
import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

class TransientArrayMapTest : FreeSpec({
    "ctor() map size less than or equal to HASHTABLE_THRESHOLD" {
        val array: Array<Any?> = arrayOf("a", 1, "b", 2, "c", 3)

        val tam = PersistentArrayMap.TransientArrayMap<String, Int>(array)

        tam.array shouldNotBeSameInstanceAs array
        tam.array.size shouldBeExactly HASHTABLE_THRESHOLD
        tam.length shouldBeExactly array.size
        tam.edit.shouldNotBeNull()
        shouldNotThrow<Exception> { tam.ensureEditable() }
    }

    "ctor() map size greater than HASHTABLE_THRESHOLD" {
        val l = (0..32).flatMap { listOf("$it", it) }
        val array: Array<Any?> = l.toTypedArray()

        val tam = PersistentArrayMap.TransientArrayMap<String, Int>(array)

        tam.array shouldNotBeSameInstanceAs array
        tam.array.size shouldBeExactly l.size
        tam.length shouldBeExactly array.size
        tam.edit.shouldNotBeNull()
        tam.array[31] shouldBe 15
        shouldNotThrow<Exception> { tam.ensureEditable() }
    }
})
