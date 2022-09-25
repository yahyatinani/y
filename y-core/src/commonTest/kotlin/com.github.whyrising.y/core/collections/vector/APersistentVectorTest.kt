package com.github.whyrising.y.core.collections.vector

import com.github.whyrising.y.core.collections.APersistentVector
import com.github.whyrising.y.core.collections.IPersistentCollection
import com.github.whyrising.y.core.collections.IPersistentStack
import com.github.whyrising.y.core.collections.IPersistentVector
import com.github.whyrising.y.core.collections.PersistentList
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
