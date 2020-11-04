package com.github.whyrising.y.mocks

import com.github.whyrising.y.IHashEq

class HashEqMock : IHashEq {
    override fun hasheq(): Int = 111111111
}
