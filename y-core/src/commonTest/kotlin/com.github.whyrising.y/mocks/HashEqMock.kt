package com.github.whyrising.y.mocks

import com.github.whyrising.y.collections.IHashEq

class HashEqMock : IHashEq {
    override fun hasheq(): Int = 111111111
}
