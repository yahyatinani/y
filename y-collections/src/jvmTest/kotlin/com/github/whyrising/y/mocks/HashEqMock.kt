package com.github.whyrising.y.mocks

import com.github.whyrising.y.core.IHashEq

class HashEqMock : IHashEq {
    override fun hasheq(): Int = 111111111
}
