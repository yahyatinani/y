package com.github.whyrising.y.core.mocks

import com.github.whyrising.y.core.collections.IHashEq

class HashEqMock : IHashEq {
  override fun hasheq(): Int = 111111111
}
