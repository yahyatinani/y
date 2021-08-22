package com.github.whyrising.y.collections

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

class Edit(value: Any?) {
    private val _value: AtomicRef<Any?> = atomic(value)

    var value by _value
}
