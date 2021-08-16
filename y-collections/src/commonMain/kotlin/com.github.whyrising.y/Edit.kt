package com.github.whyrising.y

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

class Edit(value: Any?) {
    private val lock = reentrantLock()

    var value: Any? = value
        get() {
            lock.withLock {
                return field
            }
        }
        internal set(value) {
            lock.withLock {
                field = value
            }
        }
}
