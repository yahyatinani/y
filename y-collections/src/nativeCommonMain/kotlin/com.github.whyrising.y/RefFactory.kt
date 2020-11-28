package com.github.whyrising.y

import kotlin.native.ref.WeakReference

actual object RefFactory {
    actual fun <T : Any> create(any: T): Ref<T> = RefImpl(any)
}

class RefImpl<T : Any>(any: T) : Ref<T> {
    internal val weakReference: WeakReference<T> = WeakReference(any)

    override val value: T?
        get() = weakReference.value
}
