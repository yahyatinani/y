package com.github.whyrising.y.collections.mocks

import com.github.whyrising.y.collections.core.IHashEq

class HashEqMock : IHashEq {
    override fun hasheq(): Int = 111111111
}
