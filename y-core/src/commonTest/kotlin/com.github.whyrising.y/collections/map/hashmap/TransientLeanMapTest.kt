package com.github.whyrising.y.collections.map.hashmap

import com.github.whyrising.y.collections.concretions.map.PersistentHashMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.BitMapIndexedNode
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.EmptyHashMap
import com.github.whyrising.y.collections.concretions.map.PersistentHashMap.TransientLeanMap
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

class TransientLeanMapTest : FreeSpec({
    "ctor" {
        val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

        trlm.edit.shouldNotBeNull()
        trlm.root.shouldBeNull()
        trlm.countValue shouldBeExactly 0
        trlm.leafFlag.value.shouldBeNull()
    }

    "doPersistent()" - {
        @Suppress("UNCHECKED_CAST")
        "when transient is mutable, it should return the LeanMap of it" {
            val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

            val map = trlm.doPersistent() as PersistentHashMap<String, Int>

            map.count shouldBeExactly 0
            map.root.shouldBeNull()
        }
    }

    """assertMutable() when called after the call of persistent(),
       it should throw ane exception """ {
        val trlm1 = TransientLeanMap<String, Int>(EmptyHashMap)
        val trlm2 = TransientLeanMap<String, Int>(EmptyHashMap)
        trlm2.doPersistent()

        shouldNotThrow<Exception> { trlm1.ensureEditable() }
        shouldThrowExactly<IllegalStateException> {
            trlm2.ensureEditable()
        }.message shouldBe "Transient used after persistent() call."
    }

    "doCount" {
        val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

        trlm.doCount shouldBeExactly 0
    }

    "doAssoc(key,val)" {
        val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

        val r1 = trlm.doAssoc("a", 1) as TransientLeanMap<String, Int>
        val root1 = r1.root as BitMapIndexedNode<String, Int>

        r1 shouldBeSameInstanceAs trlm
        r1.leafFlag.value.shouldNotBeNull()
        r1.count shouldBeExactly 1
        r1.root shouldBeSameInstanceAs trlm.root
        root1.array[0] shouldBe "a"
        root1.array[1] shouldBe 1

        val r2 = r1.doAssoc("b", 2) as TransientLeanMap<String, Int>
        val root2 = r2.root as BitMapIndexedNode<String, Int>

        r2.leafFlag.value.shouldNotBeNull()
        r2 shouldBeSameInstanceAs trlm
        r2.count shouldBeExactly 2
        r2.root shouldBeSameInstanceAs r1.root

        root2.array[0] shouldBe "a"
        root2.array[1] shouldBe 1
        root2.array[2] shouldBe "b"
        root2.array[3] shouldBe 2
    }

    "doDissoc(key)" {
        val tm = TransientLeanMap<String, Int>(EmptyHashMap)
        val trlm = tm.assoc("a", 1).assoc("b", 2).assoc("c", 3) as
            TransientLeanMap<String, Int>

        val result = trlm.doDissoc("b") as TransientLeanMap<String, Int>
        val root = result.root
            as BitMapIndexedNode<String, Int>

        result.leafFlag.value.shouldNotBeNull()
        result.count shouldBeExactly 2
        root.array.size shouldBeExactly 4
        root.array[0] shouldBe "a"
        root.array[1] shouldBe 1
        root.array[2] shouldBe "c"
        root.array[3] shouldBe 3
    }

    "doValAt(key, default)" {
        val default = -1
        val tm = TransientLeanMap<String, Int>(EmptyHashMap)
        val trlm = tm.assoc("a", 1).assoc("b", 2).assoc("c", 3) as
            TransientLeanMap<String, Int>

        trlm.doValAt("x", default) shouldBe default
        trlm.doValAt("a", default) shouldBe 1
        trlm.doValAt("b", default) shouldBe 2
        trlm.doValAt("c", default) shouldBe 3
    }
})
