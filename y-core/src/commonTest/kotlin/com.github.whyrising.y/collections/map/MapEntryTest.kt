package com.github.whyrising.y.collections.map

import com.github.whyrising.y.collections.concretions.map.MapEntry
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.EmptyVector
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class MapEntryTest : FreeSpec({
    "key/value" {
        val entry: IMapEntry<String, Int> = MapEntry("a", 1)

        entry.key shouldBe "a"
        entry.value shouldBeExactly 1
    }

    "nth(index)" {
        val entry = MapEntry("a", 1)

        entry.nth(0) shouldBe "a"
        entry.nth(1) shouldBe 1
        val e = shouldThrowExactly<IndexOutOfBoundsException> {
            entry.nth(2)
        }

        e.message shouldBe "index = 2"
    }

    "count" {
        MapEntry("a", 1).count shouldBeExactly 2
    }

    "empty()" {
        val entry = MapEntry("a", 1)

        entry.empty() shouldBeSameInstanceAs EmptyVector
    }

    "conj(e)" {
        val entry = MapEntry("a", 1)

        val vec = entry.conj(15)

        vec.count shouldBeExactly 3
        vec.nth(0) shouldBe "a"
        vec.nth(1) shouldBe 1
        vec.nth(2) shouldBe 15
    }

    "assocN(index, value)" {
        val entry = MapEntry("a", 1)

        entry.assocN(0, "b").nth(0) shouldBe "b"
        entry.assocN(1, 15).nth(1) shouldBe 15
        entry.assocN(2, 15).nth(2) shouldBe 15
        val e = shouldThrowExactly<IndexOutOfBoundsException> {
            entry.assocN(3, 15)
        }

        e.message shouldBe "index = 3"
    }

    "iterator()" {
        val entry = MapEntry("a", 1)

        val iter = entry.iterator()

        iter.hasNext().shouldBeTrue()

        iter.next() shouldBe "a"
        iter.next() shouldBe 1

        iter.hasNext().shouldBeFalse()
        shouldThrowExactly<NoSuchElementException> { iter.next() }
    }

    @Suppress("UNCHECKED_CAST")
    "pop()" {
        val entry = MapEntry("a", 1)

        val r = entry.pop() as PersistentVector<String>

        r.count shouldBeExactly 1
        r[0] shouldBe "a"
    }
})
