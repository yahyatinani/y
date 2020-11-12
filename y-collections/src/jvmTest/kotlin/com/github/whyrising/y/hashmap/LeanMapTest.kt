package com.github.whyrising.y.hashmap

import com.github.whyrising.y.Associative
import com.github.whyrising.y.LeanMap
import com.github.whyrising.y.LeanMap.BitMapIndexedNode
import com.github.whyrising.y.LeanMap.Companion.bitpos
import com.github.whyrising.y.LeanMap.EmptyLeanMap
import com.github.whyrising.y.LeanMap.NodeSeq
import com.github.whyrising.y.LeanMap.TransientLeanMap
import com.github.whyrising.y.MapEntry
import com.github.whyrising.y.PersistentList.Empty
import com.github.whyrising.y.hasheq
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

@ExperimentalStdlibApi
class LeanMapTest : FreeSpec({
    "mask(hash, shift)" {
        LeanMap.mask(hasheq("a"), 0) shouldBeExactly 17
        LeanMap.mask(hasheq("b"), 0) shouldBeExactly 22
    }

    "bitpos(hash, shift)" {
        bitpos(hasheq("b"), 0) shouldBeExactly 4194304
        bitpos(hasheq("c"), 0) shouldBeExactly 1073741824
        bitpos(hasheq("d"), 0) shouldBeExactly 1
    }

    @Suppress("UNCHECKED_CAST")
    "asTransient()" {
        val leanMap = LeanMap<String, Int>()

        val t = leanMap.asTransient() as TransientLeanMap<String, Int>

        t.count shouldBeExactly 0
        t.root.value.shouldBeNull()
    }

    "invoke(...pairs)" {
        LeanMap("a" to 1).count shouldBeExactly 1
        LeanMap("a" to 1, "b" to 2).count shouldBeExactly 2
        LeanMap("a" to 1, "b" to 2, "c" to 3).count shouldBeExactly 3
    }

    "empty() should return EmptyLeanMap" {
        val map = LeanMap("a" to 1)

        (map.empty() === EmptyLeanMap).shouldBeTrue()
    }

    "entryAt(key)" {
        val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

        LeanMap<String, Int>().entryAt("a").shouldBeNull()
        map.entryAt("x").shouldBeNull()
        map.entryAt("a") shouldBe MapEntry("a", 1)
        map.entryAt("b") shouldBe MapEntry("b", 2)
    }

    "valAt(key, default)" {
        val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

        LeanMap<String, Int>().valAt("x", -1) shouldBe -1
        map.valAt("x", -1) shouldBe -1
        map.valAt("a", -1) shouldBe 1
        map.valAt("c", -1) shouldBe 3
    }

    "valAt(key)" {
        val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

        LeanMap<String, Int>().valAt("x").shouldBeNull()
        map.valAt("x").shouldBeNull()
        map.valAt("a") shouldBe 1
        map.valAt("c") shouldBe 3
    }

    @Suppress("UNCHECKED_CAST")
    "seq()" {
        val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

        (LeanMap<String, Int>().seq() === Empty).shouldBeTrue()
        val seq = map.seq() as NodeSeq<String, Int>

        seq.count shouldBeExactly 3
    }

    "containsKey(key)" {
        val map = LeanMap("a" to 1, "b" to 2, "c" to 3, null to 185)
        val empty = LeanMap<String, Int>() as Associative<String, Int>

        empty.containsKey("x").shouldBeFalse()
        map.containsKey("x").shouldBeFalse()
        map.containsKey("a").shouldBeTrue()
        map.containsKey(null).shouldBeTrue()
    }

    "assoc(key, value)" - {
        "when key doesn't exist, it should add the new key/value" {
            val key = "x"
            val value = 77
            val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

            val newMap1 = LeanMap<String, Int>().assoc(key, value)
            val newMap2 = map.assoc(key, value)

            newMap1.count shouldBeExactly 1
            newMap2.containsKey(key)

            newMap2.count shouldBeExactly map.count + 1
            newMap2.containsKey(key)
        }

        "when key already exists, it should update the assoc value" {
            val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

            val newMap = map.assoc("a", 77) as LeanMap<String, Int>

            newMap shouldNotBeSameInstanceAs map
            (newMap.root as BitMapIndexedNode<String, Int>)
                .isMutable.value.shouldBeFalse()
            newMap.count shouldBeExactly map.count
            newMap("a") shouldBe 77
        }
    }

    "assocNew(key, value)" - {
        "when key already exists, it should throw an exception" {
            val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

            shouldThrowExactly<RuntimeException> {
                map.assocNew("a", 77)
            }.message shouldBe "The key a is already present."
        }

        "when key is new, it should add the key/value to the map" {
            val key = "x"
            val map = LeanMap("a" to 1, "b" to 2, "c" to 3)

            val newMap = map.assocNew(key, 77)

            newMap.count shouldBeExactly map.count + 1
            newMap.containsKey(key).shouldBeTrue()
        }
    }
})
