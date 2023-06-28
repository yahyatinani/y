package io.github.yahyatinani.y.core.mocks

import io.github.yahyatinani.y.core.collections.IHashEq

class HashEqMock : IHashEq {
  override fun hasheq(): Int = 111111111
}
