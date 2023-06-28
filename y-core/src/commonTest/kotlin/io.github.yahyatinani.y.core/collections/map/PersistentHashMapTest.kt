package io.github.yahyatinani.y.core.collections.map

import io.github.yahyatinani.y.core.collections.PersistentHashMap
import io.github.yahyatinani.y.core.collections.PersistentHashMap.Companion.createWithCheck
import io.github.yahyatinani.y.core.m
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class PersistentHashMapTest : FreeSpec({
  "createWithCheck(pairs) should throw exception when duplicate keys" {
    shouldThrowExactly<IllegalArgumentException> {
      createWithCheck("a" to 1, "b" to 2, "c" to 3, "c" to 4)
    }.message shouldBe "Duplicate key: c"
  }

  "create(pairs) should last duplicate key value" {
    val m = PersistentHashMap.create("a" to 1, "b" to 2, "c" to 3, "c" to 4)

    m shouldBe m("a" to 1, "b" to 2, "c" to 4)
  }
})
