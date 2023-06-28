package io.github.yahyatinani.y.core.collections.vector

import io.github.yahyatinani.y.core.collections.APersistentVector
import io.github.yahyatinani.y.core.collections.IPersistentCollection
import io.github.yahyatinani.y.core.collections.IPersistentStack
import io.github.yahyatinani.y.core.collections.IPersistentVector
import io.github.yahyatinani.y.core.collections.PersistentList
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class MockPersistentVector : APersistentVector<Any?>() {
  override val count: Int
    get() = 0

  override fun assocN(index: Int, value: Any?): IPersistentVector<Any?> {
    TODO("Not yet implemented")
  }

  override fun conj(e: Any?): IPersistentVector<Any?> {
    TODO("Not yet implemented")
  }

  override fun nth(index: Int): Any? {
    TODO("Not yet implemented")
  }

  override fun empty(): IPersistentCollection<Any?> {
    TODO("Not yet implemented")
  }

  override fun pop(): IPersistentStack<Any?> {
    TODO("Not yet implemented")
  }
}

class APersistentVectorTest : FreeSpec({
  "seq() should return PersistentList.Empty" {
    MockPersistentVector().seq() shouldBe PersistentList.Empty
  }
})
