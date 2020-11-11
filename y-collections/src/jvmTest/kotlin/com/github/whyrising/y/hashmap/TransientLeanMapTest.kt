package com.github.whyrising.y.hashmap

import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.LeanMap.TransientLeanMap
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

@OptIn(ExperimentalStdlibApi::class)
class TransientLeanMapTest : FreeSpec({
    "ctor" {
        val trlm = TransientLeanMap<String, Int>(EmptyLeanMap)

        trlm.isMutable.value.shouldBeTrue()
        trlm.root.value.shouldBeNull()
        trlm._count.value shouldBeExactly 0
        trlm.leafFlag.value.shouldBeNull()
    }

    "doPersistent()" - {
        @Suppress("UNCHECKED_CAST")
        "when transient is mutable, it should return the LeanMap of it" {
            val trlm = TransientLeanMap<String, Int>(EmptyLeanMap)

            val map = trlm.doPersistent() as LeanMap<String, Int>

            map.count shouldBeExactly 0
            map.root.shouldBeNull()
        }
    }

    """assertMutable() when called after the call of persistent(), 
       it should throw ane exception """ {
        val trlm1 = TransientLeanMap<String, Int>(EmptyLeanMap)
        val trlm2 = TransientLeanMap<String, Int>(EmptyLeanMap)
        trlm2.doPersistent()

        shouldNotThrow<Exception> { trlm1.assertMutable() }
        shouldThrowExactly<IllegalStateException> {
            trlm2.assertMutable()
        }.message shouldBe "Transient used after persistent() call."
    }

    "doCount" {
        val trlm = TransientLeanMap<String, Int>(EmptyLeanMap)

        trlm.doCount shouldBeExactly 0
    }

    "doAssoc(key,val)" {
        val trlm = TransientLeanMap<String, Int>(EmptyLeanMap)

        val r1 = trlm.doAssoc("a", 1) as TransientLeanMap<String, Int>
        val root1 = r1.root.value as LeanMap.BitMapIndexedNode<String, Int>


        r1 shouldBeSameInstanceAs trlm
        r1.count shouldBeExactly 1
        r1.root shouldBeSameInstanceAs trlm.root
        root1.array[0] shouldBe "a"
        root1.array[1] shouldBe 1

        val r2 = r1.doAssoc("b", 2) as TransientLeanMap<String, Int>
        val root2 = r2.root.value as LeanMap.BitMapIndexedNode<String, Int>
        r2 shouldBeSameInstanceAs trlm
        r2.count shouldBeExactly 2
        r2.root shouldBeSameInstanceAs r1.root

        root2.array[0] shouldBe "a"
        root2.array[1] shouldBe 1
        root2.array[2] shouldBe "b"
        root2.array[3] shouldBe 2
    }
})
