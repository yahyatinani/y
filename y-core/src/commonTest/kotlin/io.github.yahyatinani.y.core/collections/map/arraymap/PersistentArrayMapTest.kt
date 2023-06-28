package io.github.yahyatinani.y.core.collections.map.arraymap

import io.github.yahyatinani.y.core.collections.MapEntry
import io.github.yahyatinani.y.core.collections.PersistentArrayMap
import io.github.yahyatinani.y.core.collections.PersistentArrayMap.Companion.EmptyArrayMap
import io.github.yahyatinani.y.core.collections.PersistentArrayMap.Companion.createWithCheck
import io.github.yahyatinani.y.core.collections.PersistentArrayMap.TransientArrayMap
import io.github.yahyatinani.y.core.collections.PersistentHashMap
import io.github.yahyatinani.y.core.collections.PersistentHashMap.TransientLeanMap
import io.github.yahyatinani.y.core.collections.PersistentList.Empty
import io.github.yahyatinani.y.core.l
import io.github.yahyatinani.y.core.m
import io.github.yahyatinani.y.core.runAction
import io.github.yahyatinani.y.core.v
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.timing.continually
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration.Companion.seconds

@ExperimentalKotest
@ExperimentalSerializationApi
class PersistentArrayMapTest : FreeSpec({
  "TransientArrayMap" - {
    val array: Array<Any?> = arrayOf("a", 1, "b", 2, "c", 3)

    "doPersistent()" {
      val tam = TransientArrayMap<String, Int>(array)

      val map = tam.doPersistent() as PersistentArrayMap<String, Int>

      tam.edit.shouldBeNull()
      map.count shouldBeExactly array.size / 2
      map.array shouldNotBeSameInstanceAs array
      map shouldBe mapOf("a" to 1, "b" to 2, "c" to 3)

      shouldThrowExactly<IllegalStateException> {
        tam.doPersistent()
      }.message shouldBe "Transient used after persistent() call."
    }

    "persistent()" {
      val tam = TransientArrayMap<String, Int>(array)

      val map = tam.persistent() as PersistentArrayMap<String, Int>

      tam.edit.shouldBeNull()
      map.count shouldBeExactly array.size / 2
      map.array shouldNotBeSameInstanceAs array
      map shouldBe m("a" to 1, "b" to 2, "c" to 3)

      shouldThrowExactly<IllegalStateException> {
        tam.persistent()
      }.message shouldBe "Transient used after persistent() call."
    }

    "doAssoc(k,v)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.doAssoc("x", 99)
        }.message shouldBe "Transient used after persistent() call."
      }

      "when the key is new, it should add it to the map" - {
        "when length < array.length, return a TransientArrayMap" {
          val tam = TransientArrayMap<String, Int>(array)

          val newTam = tam.doAssoc("d", 4) as
            TransientArrayMap<String, Int>
          val pairs = newTam.array

          pairs[0] shouldBe "a"
          pairs[1] shouldBe 1

          pairs[6] shouldBe "d"
          pairs[7] shouldBe 4
        }

        @Suppress("UNCHECKED_CAST")
        "when length >= array.length, return TransientLeanMap" {
          val size = 16
          val a: Array<Any?> = arrayOfNulls(size)

          for (i in 0 until size step 2) {
            a[i] = "$i"
            a[i + 1] = i
          }
          val tam = TransientArrayMap<String, Int>(a)

          val newTam = tam.doAssoc("a", 74)
            as TransientLeanMap<String, Int>

          newTam.count shouldBeExactly tam.count + 1
          newTam.containsKey("a")
        }
      }

      """when map already has the key but different value,
                   it should update the value""" {
        val key = 2
        val value = "78"
        val a: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
        val tam = TransientArrayMap<Number, String>(a)

        val newTam = tam.doAssoc(key, value) as TransientArrayMap<Any, String>
        val pairs = newTam.array

        tam.length shouldBeExactly a.size

        pairs[0] shouldBe 1L
        pairs[1] shouldBe "1"

        pairs[2] shouldBe key
        pairs[3] shouldBe value

        pairs[4] shouldBe 3
        pairs[5] shouldBe "3"
      }

      """when map already has the same key/value,
                   it should return the same map""" {
        val key = 2
        val value = "2"
        val a: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
        val tam = TransientArrayMap<Number, String>(a)

        val newTam = tam.doAssoc(key, value)
          as TransientArrayMap<Any, String>

        tam shouldBeSameInstanceAs newTam
        newTam.array[0].shouldBeInstanceOf<Long>()
        newTam.array[2].shouldBeInstanceOf<Long>()
      }
    }

    "assoc(k,v)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.assoc("x", 99)
        }.message shouldBe "Transient used after persistent() call."
      }

      "when the key is new, it should add it to the map" - {
        "when length < array.length, return a TransientArrayMap" {
          val tam = TransientArrayMap<String, Int>(array)

          val newTam = tam.assoc("d", 4) as
            TransientArrayMap<String, Int>
          val pairs = newTam.array

          pairs[0] shouldBe "a"
          pairs[1] shouldBe 1

          pairs[6] shouldBe "d"
          pairs[7] shouldBe 4
        }

        @Suppress("UNCHECKED_CAST")
        "when length >= array.length, return PersistentHashMap" {
          val size = 16
          val a: Array<Any?> = arrayOfNulls(size)
          var i = 0
          while (i < size) {
            a[i] = Pair("$i", i)
            i++
          }
          val tam = TransientArrayMap<String, Int>(a)

          val newTam = tam.assoc("a", 74) as TransientLeanMap<String, Int>

          newTam.count shouldBeExactly tam.count + 1
          newTam.containsKey("a")
        }
      }

      """when map already has the key but different value,
                   it should update the value""" {
        val key = 2
        val value = "78"
        val a: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
        val tam = TransientArrayMap<Number, String>(a)

        val newTam = tam.assoc(key, value)
          as TransientArrayMap<Any, String>
        val pairs = newTam.array

        tam.length shouldBeExactly a.size

        pairs[0] shouldBe 1L
        pairs[1] shouldBe "1"

        pairs[2] shouldBe key
        pairs[3] shouldBe value

        pairs[4] shouldBe 3
        pairs[5] shouldBe "3"
      }

      """when map already has the same key/value,
                   it should return the same map""" {
        val key = 2
        val value = "2"
        val a: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
        val tam = TransientArrayMap<Any, String>(a)

        val newTam = tam.assoc(key, value)
          as TransientArrayMap<Any, String>

        tam shouldBeSameInstanceAs newTam
        newTam.array[0].shouldBeInstanceOf<Long>()
        newTam.array[2].shouldBeInstanceOf<Long>()
        newTam.array[4].shouldBeInstanceOf<Int>()
      }
    }

    @Suppress("UNCHECKED_CAST")
    "conj(e)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.conj(Pair("x", 99))
        }.message shouldBe "Transient used after persistent() call."
      }

      "when entry is a Map.Entry, it should call assoc() on it" {
        val tam = TransientArrayMap<String, Int>(array)

        val newTam = tam.conj(MapEntry("a", 99))
          as TransientArrayMap<String, Int>

        newTam.length shouldBeExactly array.size
        newTam.array[1] shouldBe 99
        newTam.array[3] shouldBe 2
        newTam.array[5] shouldBe 3
      }

      "when entry is a IPersistentVector" - {
        "when count != 2, it should throw" {
          val tam = TransientArrayMap<String, Int>(array)

          shouldThrowExactly<IllegalArgumentException> {
            tam.conj(v("a", 99, 75))
          }.message shouldBe
            "Vector [a 99 75] count must be 2 to conj in a map."
        }

        "when count == 2, it should call assoc() on it" {
          val tam = TransientArrayMap<String, Int>(array)

          val newMap = tam.conj(v("a", 99))
            as TransientArrayMap<String, Int>

          newMap.length shouldBeExactly array.size
          newMap.array[1] shouldBe 99
          newMap.array[3] shouldBe 2
        }
      }

      "when entry is null, it should return this" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.conj(null) shouldBeSameInstanceAs tam
      }

      "when entry is a seq of MapEntry" - {
        "when an element is not a MapEntry, it should throw" {
          val tam = TransientArrayMap<String, Int>(array)

          shouldThrowExactly<IllegalArgumentException> {
            tam.conj(l(MapEntry("x", 42), "item"))
          }.message shouldBe
            "All elements of the seq must be of type " +
            "Map.Entry to conj: item"
        }

        "when all elements are MapEntry, it should assoc() all" {
          val tam = TransientArrayMap<String, Int>(array)
          val entries = l(MapEntry("x", 42), MapEntry("y", 47))

          val newTam = tam.conj(entries)
            as TransientArrayMap<String, Int>

          newTam.length shouldBeExactly
            array.size + (entries.count * 2)

          newTam.array[0] shouldBe "a"
          newTam.array[1] shouldBe 1

          newTam.array[2] shouldBe "b"
          newTam.array[3] shouldBe 2

          newTam.array[6] shouldBe "x"
          newTam.array[7] shouldBe 42

          newTam.array[8] shouldBe "y"
          newTam.array[9] shouldBe 47
        }
      }
    }

    @Suppress("UNCHECKED_CAST")
    "doDissoc(key)" - {
      "when key doesn't exit, it should return the same instance" {
        val a: Array<Any?> = arrayOf(1, "a", 2, "b", 3, "c")
        val tam = TransientArrayMap<Int, String>(a)

        val dissoc = tam.doDissoc(10)

        dissoc shouldBeSameInstanceAs tam
      }

      "when key exists and size is 1, it returns an empty transient" {
        val a: Array<Any?> = arrayOf(2L, "b")
        val tam = TransientArrayMap<Number, String>(a)

        val rTam = tam.doDissoc(2) as TransientArrayMap<Int, String>

        rTam.length shouldBeExactly 0
      }

      "when key exists, it should return a new map without that key" {
        val a: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
        val tam = TransientArrayMap<Number, String>(a)

        val rTam = tam.doDissoc(1) as TransientArrayMap<Any?, String>

        rTam.length shouldBeExactly a.size - 2

        rTam.array[0] shouldBe 3
        rTam.array[1] shouldBe "3"

        rTam.array[2] shouldBe 2L
        rTam.array[3] shouldBe "2"
      }
    }

    @Suppress("UNCHECKED_CAST")
    "dissoc(key)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.dissoc("a")
        }.message shouldBe "Transient used after persistent() call."
      }

      "when key doesn't exit, it should return the same instance" {
        val a: Array<Any?> = arrayOf(1, "a", 2, "b", 3, "c")
        val tam = TransientArrayMap<Int, String>(a)

        val dissoc = tam.dissoc(10)

        dissoc shouldBeSameInstanceAs tam
      }

      "when key exists and size is 1, it returns an empty transient" {
        val a: Array<Any?> = arrayOf(2L, "b")
        val tam = TransientArrayMap<Number, String>(a)

        val rTam = tam.dissoc(2) as TransientArrayMap<Int, String>

        rTam.length shouldBeExactly 0

//                for (i in rTam.array.indices)
//                    rTam.array[i].shouldBeNull()
      }

      "when key exists, it should return a new map without that key" {
        val a: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
        val tam = TransientArrayMap<Number, String>(a)

        val rTam = tam.dissoc(1) as TransientArrayMap<Any?, String>
        val pairs = rTam.array

        rTam.length shouldBeExactly a.size - 2
        pairs[0] shouldBe 3
        pairs[1] shouldBe "3"

        pairs[2] shouldBe 2L
        pairs[3] shouldBe "2"
      }
    }

    "doCount" {
      val a: Array<Any?> = emptyArray()
      val b: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")
      val tam = TransientArrayMap<Number, String>(b)
        .dissoc(1) as TransientArrayMap<*, *>

      TransientArrayMap<Number, String>(a).doCount shouldBeExactly 0

      TransientArrayMap<Number, String>(b).doCount shouldBeExactly
        b.size / 2

      tam.doCount shouldBeExactly 2
    }

    "count" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.count
        }.message shouldBe "Transient used after persistent() call."
      }

      "assertions" {
        val a: Array<Any?> = arrayOf()
        val b: Array<Any?> = arrayOf(1L, "1", 2L, "2", 3, "3")

        TransientArrayMap<Number, String>(a).count shouldBeExactly 0

        TransientArrayMap<Number, String>(b)
          .count shouldBeExactly b.size / 2

        TransientArrayMap<Number, String>(b)
          .dissoc(1).count shouldBeExactly 2
      }
    }

    "doValAt(key, default)" - {
      val tam = TransientArrayMap<String, Int>(array)

      "when key exists, it should return the assoc value" {
        tam.doValAt("a", -1) shouldBe 1
      }

      "when key doesn't exist, it should return the default value" {
        tam.doValAt("z", -1) shouldBe -1
      }
    }

    "valAt(key, default)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.valAt("a", -1)
        }.message shouldBe "Transient used after persistent() call."
      }

      "when key exists, it should return the assoc value" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.valAt("a", -1) shouldBe 1
      }

      "when key doesn't exist, it should return the default value" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.valAt("z", -1) shouldBe -1
      }
    }

    "valAt(key)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.valAt("a")
        }.message shouldBe "Transient used after persistent() call."
      }

      "when key exists, it should return the assoc value" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.valAt("a") shouldBe 1
      }

      "when key doesn't exist, it should return the default value" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.valAt("z").shouldBeNull()
      }
    }

    "containsKey(key)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)
        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.containsKey("a")
        }.message shouldBe "Transient used after persistent() call."
      }

      "assertions" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.containsKey("a").shouldBeTrue()
        tam.containsKey("b").shouldBeTrue()

        tam.containsKey("d").shouldBeFalse()
      }
    }

    "entryAt(key)" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)
        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.entryAt("a")
        }.message shouldBe "Transient used after persistent() call."
      }

      "when key doesn't exit, it should return null" {
        val tam = TransientArrayMap<String?, Int>(array)

        tam.entryAt(null).shouldBeNull()
      }

      "when key does exist, it should return a MapEntry" {
        val tam = TransientArrayMap<String, Int>(array)

        val mapEntry = tam.entryAt("a") as MapEntry<String, Int>

        mapEntry.key shouldBe "a"
        mapEntry.value shouldBe 1
      }
    }

    "invoke() operator" - {
      "when called after calling persistent, it should throw" {
        val tam = TransientArrayMap<String, Int>(array)

        tam.persistent()

        shouldThrowExactly<IllegalStateException> {
          tam.entryAt("a")
        }.message shouldBe "Transient used after persistent() call."
      }

      val a = arrayOf("a" to 1, "b" to 2, "c" to 3)
      val tam = TransientArrayMap<String, Int>(array)

      "invoke(key, default)" {
        tam("a", -1) shouldBe 1
        tam("z", -1) shouldBe -1
      }

      "invoke(key)" {
        tam("a") shouldBe 1
        tam("z").shouldBeNull()
      }
    }

    "concurrency" - {
      val l = (1..8).map { MapEntry(it, "$it") }

      "assoc(key, val) under 16 entries-" {
        continually(10.seconds) {
          val t1 = EmptyArrayMap.asTransient()
            as TransientArrayMap<Int, String>

          withContext(Dispatchers.Default) {
            runAction(16, 3) {
              for (entry in l)
                t1.assoc(entry.key, entry.value)
            }
          }

          t1.count shouldBeExactly 8
          val m = t1.persistent() as PersistentArrayMap<Int, String>
          m.shouldContainAll(l)
        }
      }

      "dissoc(key)" {
        continually(10.seconds) {
          val initial = m<Any?, Any?>()
          val transientMap = l.fold(initial) { map, entry ->
            map.assoc(
              entry.key,
              entry.value,
            ) as PersistentArrayMap<Int, String>
          }.asTransient()

          withContext(Dispatchers.Default) {
            runAction(16, 3) {
              for (entry in l)
                transientMap.dissoc(entry.key)
            }
          }
          val persistent = transientMap.persistent()

          persistent.count shouldBeExactly 0
          persistent.shouldNotContainAnyOf(l)
        }
      }
    }
  }

  "asTransient()" {
    val a = arrayOf("a" to 1, "b" to 2, "c" to 3)
    val map = createWithCheck(*a)

    val tr = map.asTransient() as TransientArrayMap<String, Int>
    val array = tr.array

    tr.length shouldBeExactly a.size * 2
    array.size shouldBeExactly 16
    array[0] shouldBe "a"
    array[1] shouldBe 1
    array[2] shouldBe "b"
    array[3] shouldBe 2
    array[4] shouldBe "c"
    array[5] shouldBe 3
  }

  "ArrayMap" - {
    "assoc(key, val)" - {
      "when map is empty, it should add the new entry" {
        val map = m<Any?, Any?>()

        val newMap =
          map.assoc("a", 1) as PersistentArrayMap<String, Int>
        val pairs = newMap.array

        pairs[0] shouldBe "a"
        pairs[1] shouldBe 1
      }

      "when the key is new, it should add it to the map" - {
        "when size < threshold, it should return a PersistentArrayMap" {
          val map = m("a" to 1, "b" to 2, "c" to 3)

          val newMap = map.assoc("d", 4)
            as PersistentArrayMap<String, Int>

          newMap.array[0] shouldBe "a"
          newMap.array[1] shouldBe 1

          newMap.array[6] shouldBe "d"
          newMap.array[7] shouldBe 4
        }

        @Suppress("UNCHECKED_CAST")
        "when size >= THRESHOLD, it should return LeanMap" {
          val size = 16
          val array: Array<Pair<String, Int>?> = arrayOfNulls(size)
          var i = 0
          while (i < size) {
            array[i] = Pair("$i", i)
            i++
          }
          val m = m(*(array as Array<Pair<String, Int>>))

          val map = m.assoc("a", 863)
            as PersistentHashMap<String, Int>

          m.containsKey("a").shouldBeFalse()

          map.count shouldBeExactly size + 1
          map.containsKey("a").shouldBeTrue()
        }
      }

      """when map already has the key and different value,
               it should replace it in a new map""" {
        val key = 2
        val value = "78"
        val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
        val map = m(*array)

        val newMap = map.assoc(key, value)
          as PersistentArrayMap<String, Int>

        newMap.array.size shouldBeExactly array.size * 2

        array[1].first shouldBe key
        array[1].second shouldBe "2"

        newMap.array[0] shouldBe 1L
        newMap.array[1] shouldBe "1"
        newMap.array[2] shouldBe key
        newMap.array[3] shouldBe value
        newMap.array[4] shouldBe 3
        newMap.array[5] shouldBe "3"
      }

      """when map already has the same key/value,
               it should return the same map""" {
        val key = 2
        val value = "2"
        val map = m(1L to "1", 2L to "2", 3 to "3")

        val newMap = map.assoc(key, value)
          as PersistentArrayMap<String, Int>

        newMap shouldBeSameInstanceAs map
        newMap.array[2].shouldBeInstanceOf<Long>()
      }
    }

    "assocNew(key, val)" - {
      "when map already has the key, it should throw" {
        val value = "78"
        val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
        val map = m(*array)

        shouldThrowExactly<RuntimeException> {
          map.assocNew(2, value)
        }.message shouldBe "The key 2 is already present."
      }

      "when new key, it should add the association to the new map" {
        val key = 4
        val value = "4"
        val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
        val map = m(*array)

        val newMap = map.assocNew(key, value)
          as PersistentArrayMap<String, Int>

        newMap.array.size shouldBeExactly (array.size * 2) + 2

        newMap.array[0] shouldBe key
        newMap.array[1] shouldBe value
        newMap.array[2] shouldBe 1L
        newMap.array[3] shouldBe "1"
        newMap.array[6] shouldBe 3
        newMap.array[7] shouldBe "3"
      }

      @Suppress("UNCHECKED_CAST")
      "when size >= THRESHOLD, it should return LeanMap" {
        val size = 16
        val array: Array<Pair<String, Int>?> = arrayOfNulls(size)
        for (i in 0 until size) {
          array[i] = Pair("$i", i)
        }
        val m = m(*(array as Array<Pair<String, Int>>))

        val map = m.assocNew("a", 863) as PersistentHashMap<String, Int>

        m.containsKey("a").shouldBeFalse()

        map.count shouldBeExactly size + 1
        map.containsKey("a").shouldBeTrue()
        shouldThrowExactly<RuntimeException> {
          map.assocNew("a", -1)
        }.message shouldBe "The key a is already present."
      }
    }

    "dissoc(key)" - {
      "when key doesn't exit, it should return the same instance" {
        val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
        val map = m(*array)

        map.dissoc(9) shouldBeSameInstanceAs map
      }

      "when key exists and size is 1, it should return the empty map" {
        val map = m(2L to "2")

        map.dissoc(2) shouldBeSameInstanceAs EmptyArrayMap
      }

      "when key exists, it should return a new map without that key" {
        val array = arrayOf(1L to "1", 2L to "2", 3 to "3")
        val map = m(*array)

        val newMap = map.dissoc(2) as PersistentArrayMap<Any?, String>

        newMap.array.size shouldBeExactly 4
        newMap.array[0] shouldBe array[0].first
        newMap.array[1] shouldBe array[0].second
        newMap.array[2] shouldBe array[2].first
        newMap.array[3] shouldBe array[2].second
      }
    }

    "containsKey(key)" {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
      val map = m(*array)

      map.containsKey("a").shouldBeTrue()
      map.containsKey("b").shouldBeTrue()

      map.containsKey("d").shouldBeFalse()
    }

    "entryAt(key)" - {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
      val map = m(*array)

      "when key doesn't exit, it should return null" {
        map.entryAt("d").shouldBeNull()
      }

      "when key does exist, it should return a MapEntry" {
        val mapEntry = map.entryAt("a") as MapEntry<String, Int>

        mapEntry.key shouldBe "a"
        mapEntry.value shouldBe 1
      }
    }

    "valAt(key, default)" - {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
      val map = m(*array)

      "when key exists, it should return the assoc value" {
        map.valAt("a", -1) shouldBe 1
      }

      "when key doesn't exist, it should return the default value" {
        map.valAt("z", -1) shouldBe -1
      }
    }

    "valAt(key)" - {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
      val map = m(*array)

      "when key exists, it should return the assoc value" {
        map.valAt("a") shouldBe 1
      }

      "when key doesn't exist, it should return the default value" {
        map.valAt("z").shouldBeNull()
      }
    }

    "seq()" - {
      "when map is empty, it should return an empty seq" {
        m<Any?, Any?>().seq() shouldBeSameInstanceAs
          Empty
      }

      "when map is populated, it should return a seq of entries" {
        val array = arrayOf("a" to 1)
        val map = createWithCheck(*array)

        val seq = map.seq()
        val rest = seq.rest()

        seq.toString() shouldBe "([a 1])"

        seq.count shouldBeExactly map.size

        seq.first() shouldBe MapEntry("a", 1)

        rest shouldBeSameInstanceAs Empty
      }
    }

    "count" {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

      m<Any?, Any?>().count shouldBeExactly 0
      m(*array).count shouldBeExactly array.size
    }

    "empty()" {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)

      m(*array).empty() shouldBeSameInstanceAs
        EmptyArrayMap
    }

    "iterator()" {
      val array = arrayOf("a" to 1, "b" to 2, "c" to 3)
      val map = m(*array)
      val iter = map.iterator()

      iter.hasNext().shouldBeTrue()

      iter.next() shouldBe MapEntry("a", 1)
      iter.next() shouldBe MapEntry("b", 2)
      iter.next() shouldBe MapEntry("c", 3)

      iter.hasNext().shouldBeFalse()

      shouldThrowExactly<NoSuchElementException> { iter.next() }
    }

    "keyIterator()" {
      val map = createWithCheck("a" to 1, "b" to 2, "c" to 3)

      val iter: Iterator<String> = map.keyIterator()

      iter.hasNext().shouldBeTrue()

      iter.next() shouldBe "a"
      iter.next() shouldBe "b"
      iter.next() shouldBe "c"

      iter.hasNext().shouldBeFalse()

      shouldThrowExactly<NoSuchElementException> { iter.next() }
    }

    "valIterator()" {
      val map = createWithCheck("a" to 1, "b" to 2, "c" to 3)

      val iter: Iterator<Int> = map.valIterator()

      iter.hasNext().shouldBeTrue()

      iter.next() shouldBeExactly 1
      iter.next() shouldBeExactly 2
      iter.next() shouldBeExactly 3

      iter.hasNext().shouldBeFalse()

      shouldThrowExactly<NoSuchElementException> { iter.next() }
    }
  }

  "EmptyArrayMap" - {
    "toString() should return `{}`" {
      m<Any?, Any?>().toString() shouldBe "{}"
    }

    "hashCode()" {
      m<Any?, Any?>().hashCode() shouldBeExactly 0
    }
  }
})
