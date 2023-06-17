package com.github.whyrising.y.core.collections.map.hashmap

import com.github.whyrising.y.core.collections.Associative
import com.github.whyrising.y.core.collections.MapEntry
import com.github.whyrising.y.core.collections.PersistentHashMap
import com.github.whyrising.y.core.collections.PersistentHashMap.BitMapIndexedNode
import com.github.whyrising.y.core.collections.PersistentHashMap.Companion.bitpos
import com.github.whyrising.y.core.collections.PersistentHashMap.Companion.create
import com.github.whyrising.y.core.collections.PersistentHashMap.EmptyHashMap
import com.github.whyrising.y.core.collections.PersistentHashMap.NodeIterator
import com.github.whyrising.y.core.collections.PersistentHashMap.NodeIterator.EmptyNodeIterator
import com.github.whyrising.y.core.collections.PersistentHashMap.NodeSeq
import com.github.whyrising.y.core.collections.PersistentHashMap.TransientLeanMap
import com.github.whyrising.y.core.collections.PersistentList.Empty
import com.github.whyrising.y.core.hashMap
import com.github.whyrising.y.core.util.hasheq
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.random.Random

@ExperimentalSerializationApi
class LeanMapTest : FreeSpec({
  "mask(hash, shift)" {
    PersistentHashMap.mask(hasheq("a"), 0) shouldBeExactly 17
    PersistentHashMap.mask(hasheq("b"), 0) shouldBeExactly 22
  }

  "bitpos(hash, shift)" {
    bitpos(hasheq("b"), 0) shouldBeExactly 4194304
    bitpos(hasheq("c"), 0) shouldBeExactly 1073741824
    bitpos(hasheq("d"), 0) shouldBeExactly 1
  }

  @Suppress("UNCHECKED_CAST")
  "asTransient()" {
    val leanMap = PersistentHashMap<String, Int>()

    val t = leanMap.asTransient() as TransientLeanMap<String, Int>

    t.count shouldBeExactly 0
    t.root.shouldBeNull()
  }

  "create(map)" {
    val map1 = create(mapOf("a" to 1))
    val map2 = create(mapOf("a" to 1, "b" to 2))
    val map3 = create(mapOf("a" to 1, "b" to 2, "c" to 3))

    map1.count shouldBeExactly 1
    map1.containsKey("a")

    map2.count shouldBeExactly 2
    map2.containsKey("a")
    map2.containsKey("b")

    map3.count shouldBeExactly 3
    map3.containsKey("a")
    map3.containsKey("b")
    map3.containsKey("c")
  }

  "empty() should return EmptyLeanMap" {
    val map = create("a" to 1)

    (map.empty() === EmptyHashMap).shouldBeTrue()
  }

  "entryAt(key)" {
    val map = create("a" to 1, "b" to 2, "c" to 3)

    PersistentHashMap<String, Int>().entryAt("a").shouldBeNull()
    map.entryAt("x").shouldBeNull()
    map.entryAt("a") shouldBe MapEntry("a", 1)
    map.entryAt("b") shouldBe MapEntry("b", 2)
  }

  "valAt(key, default)" {
    val map = create("a" to 1, "b" to 2, "c" to 3)

    PersistentHashMap<String, Int>().valAt("x", -1) shouldBe -1
    map.valAt("x", -1) shouldBe -1
    map.valAt("a", -1) shouldBe 1
    map.valAt("c", -1) shouldBe 3
  }

  "valAt(key)" {
    val map = create("a" to 1, "b" to 2, "c" to 3)

    PersistentHashMap<String, Int>().valAt("x").shouldBeNull()
    map.valAt("x").shouldBeNull()
    map.valAt("a") shouldBe 1
    map.valAt("c") shouldBe 3
  }

  @Suppress("UNCHECKED_CAST")
  "seq()" {
    val map = create("a" to 1, "b" to 2, "c" to 3)

    (PersistentHashMap<String, Int>().seq() === Empty).shouldBeTrue()
    val seq = map.seq() as NodeSeq<String, Int>

    seq.count shouldBeExactly 3
  }

  "toString()" {
    hashMap<String, Int>().toString() shouldBe "{}"
  }

  "containsKey(key)" {
    val map = create("a" to 1, "b" to 2, "c" to 3, null to 185)
    val empty = PersistentHashMap<String, Int>() as Associative<String, Int>

    empty.containsKey("x").shouldBeFalse()
    map.containsKey("x").shouldBeFalse()
    map.containsKey("a").shouldBeTrue()
    map.containsKey(null).shouldBeTrue()
  }

  "assoc(key, value)" - {
    "when key doesn't exist, it should add the new key/value" {
      val key = "x"
      val value = 77
      val map = create("a" to 1, "b" to 2, "c" to 3)

      val newMap1 = PersistentHashMap<String, Int>().assoc(key, value)
      val newMap2 = map.assoc(key, value)

      newMap1.count shouldBeExactly 1
      newMap2.containsKey(key)

      newMap2.count shouldBeExactly map.count + 1
      newMap2.containsKey(key)
    }

    "when key already exists, it should update the assoc value" {
      val map = create("a" to 1, "b" to 2, "c" to 3)

      val newMap = map.assoc("a", 77) as PersistentHashMap<String, Int>

      newMap shouldNotBeSameInstanceAs map
      (newMap.root as BitMapIndexedNode<String, Int>)
        .edit.value.shouldBeNull()
      newMap.count shouldBeExactly map.count
      newMap("a") shouldBe 77
    }
  }

  "assocNew(key, value)" - {
    "when key already exists, it should throw an exception" {
      val map = create("a" to 1, "b" to 2, "c" to 3)

      shouldThrowExactly<RuntimeException> {
        map.assocNew("a", 77)
      }.message shouldBe "The key a is already present."
    }

    "when key is new, it should add the key/value to the map" {
      val key = "x"
      val map = create("a" to 1, "b" to 2, "c" to 3)

      val newMap = map.assocNew(key, 77)

      newMap.count shouldBeExactly map.count + 1
      newMap.containsKey(key).shouldBeTrue()
    }
  }

  "dissoc(key)" {
    val emptyMap = PersistentHashMap<String, Int>()
    val map = create("a" to 1, "b" to 2, "c" to 3)

    val newMap0 = emptyMap.dissoc("a")
    val newMap1 = map.dissoc("a") as PersistentHashMap<String, Int>
    val newMap2 = map.dissoc("x") as PersistentHashMap<String, Int>
    val newMap3 = map.dissoc("a").dissoc("b").dissoc("c")
    val root1 = newMap1.root as BitMapIndexedNode<String, Int>
    val root2 = newMap2.root as BitMapIndexedNode<String, Int>

    map.count shouldBeExactly 3
    map.containsKey("a").shouldBeTrue()
    map.containsKey("b").shouldBeTrue()
    map.containsKey("c").shouldBeTrue()

    newMap0 shouldBeSameInstanceAs EmptyHashMap
    create("a" to 1).dissoc("a") shouldBeSameInstanceAs
      EmptyHashMap

    root1.edit.value.shouldBeNull()
    newMap1.count shouldBeExactly map.count - 1
    newMap1.containsKey("a").shouldBeFalse()

    root2.edit.value.shouldBeNull()
    newMap2 shouldBeSameInstanceAs map
    newMap2.count shouldBeExactly map.count

    newMap3.count shouldBeExactly 0
    newMap3 shouldBeSameInstanceAs EmptyHashMap
  }

  "iterator()" - {
    "when map is EmptyLeanMap it should return EmptyIterator" {
      val emptyMap = PersistentHashMap<String, Int>()

      val iter = emptyMap.iterator() as EmptyNodeIterator

      iter.f.shouldBeNull()
      iter.node.shouldBeNull()
      iter.hasNext().shouldBeFalse()
      shouldThrowExactly<NoSuchElementException> { iter.next() }
    }

    @Suppress("UNCHECKED_CAST")
    "when map is LMap, it should return NodeIter" {
      val gen = Arb.set(Arb.string(0, 8), 0..6)
      checkAll(gen) { set: Set<String> ->
        val a = set.map { s: String -> Pair(s, Random.nextInt()) }
        val map = create(*a.toTypedArray())

        val iter =
          map.iterator() as NodeIterator<String, Int, MapEntry<String, Int>>

        var i = 0
        while (iter.hasNext()) {
          map.containsKey(iter.next().key).shouldBeTrue()
          i++
        }

        if (iter is NodeIterator.NodeIter<String, Int, MapEntry<String, Int>>) {
          iter.node shouldBeSameInstanceAs map.root
          iter._f(a[0].first, a[0].second) shouldBe MapEntry(
            a[0].first,
            a[0].second,
          )
        }

        shouldThrowExactly<NoSuchElementException> { iter.next() }

        when {
          set.isEmpty() -> i shouldBeExactly 0
          else -> i shouldBeExactly map.count
        }
      }
    }
  }

  "keyIterator()" - {
    "when map is EmptyLeanMap it should return EmptyIterator" {
      val emptyMap = PersistentHashMap<String, Int>()

      emptyMap.keyIterator() shouldBeSameInstanceAs EmptyNodeIterator
    }

    @Suppress("UNCHECKED_CAST")
    "when map is LMap, it should return NodeIter" {
      val map = create("a" to 1, "b" to 2, "c" to 3)

      val keyIter = map.keyIterator() as NodeIterator<String, Int, String>

      keyIter.next() shouldBe "a"
    }
  }

  "valIterator()" - {
    "when map is EmptyLeanMap it should return EmptyIterator" {
      val emptyMap = PersistentHashMap<String, Int>()

      emptyMap.valIterator() shouldBeSameInstanceAs EmptyNodeIterator
    }

    @Suppress("UNCHECKED_CAST")
    "when map is LMap, it should return NodeIter" {
      val map = create("a" to 1, "b" to 2, "c" to 3)

      val keyIter = map.valIterator() as NodeIterator<String, Int, Int>

      keyIter.next() shouldBe 1
    }
  }
})
