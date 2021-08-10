package com.github.whyrising.y

import kotlin.native.ref.WeakReference

actual class WeakReference<T : Any> actual constructor(ref: T) {
    internal var pointer: WeakReference<T>? = WeakReference(ref)

    actual val value: T?
        get() = pointer?.value

    actual fun clear() {
        pointer?.clear()
        pointer = null
    }
}
