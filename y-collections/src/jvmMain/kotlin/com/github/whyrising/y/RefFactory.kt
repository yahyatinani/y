package com.github.whyrising.y

import java.lang.ref.WeakReference

actual object RefFactory {
    actual fun <T : Any> create(any: T): Any = WeakReference(any)

    @Suppress("UNCHECKED_CAST")
    actual fun <T : Any> valueOf(ref: Any): T? = (ref as WeakReference<T>).get()
}
