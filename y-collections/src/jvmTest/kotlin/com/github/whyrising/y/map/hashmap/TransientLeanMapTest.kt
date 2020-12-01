package com.github.whyrising.y.map.hashmap

import com.github.whyrising.y.concretions.map.MapEntry
import com.github.whyrising.y.concretions.map.PersistentHashMap
import com.github.whyrising.y.concretions.map.PersistentHashMap.BitMapIndexedNode
import com.github.whyrising.y.concretions.map.PersistentHashMap.EmptyHashMap
import com.github.whyrising.y.concretions.map.PersistentHashMap.TransientLeanMap
import com.github.whyrising.y.map.arraymap.runAction
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalStdlibApi::class)
class TransientLeanMapTest : FreeSpec({
    "ctor" {
        val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

        trlm.isMutable.value.shouldBeTrue()
        trlm.root.value.shouldBeNull()
        trlm._count.value shouldBeExactly 0
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

        shouldNotThrow<Exception> { trlm1.assertMutable() }
        shouldThrowExactly<IllegalStateException> {
            trlm2.assertMutable()
        }.message shouldBe "Transient used after persistent() call."
    }

    "doCount" {
        val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

        trlm.doCount shouldBeExactly 0
    }

    "doAssoc(key,val)" {
        val trlm = TransientLeanMap<String, Int>(EmptyHashMap)

        val r1 = trlm.doAssoc("a", 1) as TransientLeanMap<String, Int>
        val root1 = r1.root.value as BitMapIndexedNode<String, Int>

        r1 shouldBeSameInstanceAs trlm
        r1.leafFlag.value.shouldNotBeNull()
        r1.count shouldBeExactly 1
        r1.root shouldBeSameInstanceAs trlm.root
        root1.array[0] shouldBe "a"
        root1.array[1] shouldBe 1

        val r2 = r1.doAssoc("b", 2) as TransientLeanMap<String, Int>
        val root2 = r2.root.value as BitMapIndexedNode<String, Int>

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
        val root = result.root.value
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

    "concurrency" {
        val range16 = 1..16
        val l = range16.fold(listOf<MapEntry<Int, String>>()) { coll, i ->
            coll.plus<MapEntry<Int, String>>(MapEntry(i, "$i"))
        }

        val keyCounter = atomic(0)
        val t1 = TransientLeanMap<Int, String>(EmptyHashMap)

        withContext(Dispatchers.Default) {
            runAction(100, 100) {
                val i = keyCounter.incrementAndGet()
                t1.assoc(i, "$i")
            }
        }

        t1.count shouldBeExactly 10000
        val m = t1.persistent() as PersistentHashMap<Int, String>
        m.shouldContainAll(l)

        val t2 = m.asTransient()
        withContext(Dispatchers.Default) {
            runAction(100, 100) {
                t2.dissoc(keyCounter.getAndDecrement())
            }
        }

        val persistent = t2.persistent()
        persistent.shouldNotContainAnyOf(l)
        persistent.count shouldBeExactly 0
    }
})
