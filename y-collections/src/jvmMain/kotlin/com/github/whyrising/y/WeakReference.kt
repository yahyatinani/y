package com.github.whyrising.y

import java.lang.ref.WeakReference

actual class WeakReference<T : Any> actual constructor(ref: T) {
    internal var pointer: WeakReference<T>? = WeakReference(ref)

    actual val value: T?
        get() = pointer?.get()

    actual fun clear() {
        pointer?.clear()
        pointer = null
    }
}
