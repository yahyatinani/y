package io.github.yahyatinani.y.core.collections.map

import io.github.yahyatinani.y.core.collections.PersistentArrayMap
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class PersistentArrayMapTest : FreeSpec({
  "createWithCheck(vararg pairs)" - {
    "should return a PersistentArrayMap" {
      val arrayOfPairs = arrayOf("a" to 1, "b" to 2, "c" to 3)

      val map: PersistentArrayMap<String, Int> =
        PersistentArrayMap.createWithCheck(*arrayOfPairs)

      map.array shouldBe arrayOf("a", 1, "b", 2, "c", 3)
    }

    "should throw exception when duplicate keys" {
      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck("a" to 1, "b" to 2, "b" to 3)
      }.message shouldBe "Duplicate key: b"

      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck("a" to 1, "a" to 2, "b" to 3)
      }.message shouldBe "Duplicate key: a"

      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck("a" to 1, "b" to 2, "a" to 3)
      }.message shouldBe "Duplicate key: a"

      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck(1L to "a", 1 to "b")
      }.message shouldBe "Duplicate key: 1"
    }
  }

  "createWithCheck(vararg e)" - {
    "should return a PersistentArrayMap" {
      val arrayOfPairs = arrayOf("a", 1, "b", 2, "c", 3)

      val map = PersistentArrayMap.createWithCheck<String, Int>(*arrayOfPairs)

      map.array shouldBe arrayOf("a", 1, "b", 2, "c", 3)
    }

    "should throw exception when duplicate keys" {
      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck<String, Int>("a", 1, "b", 2, "b", 3)
      }.message shouldBe "Duplicate key: b"

      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck<String, Int>("a", 1, "a", 2, "b", 3)
      }.message shouldBe "Duplicate key: a"

      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck<String, Int>("a", 1, "b", 2, "a", 3)
      }.message shouldBe "Duplicate key: a"

      shouldThrowExactly<IllegalArgumentException> {
        PersistentArrayMap.createWithCheck<Long, String>(1L, "a", 1, "b")
      }.message shouldBe "Duplicate key: 1"
    }
  }
})
