package com.github.whyrising.y

import java.lang.ref.WeakReference

class RefImp<T : Any>(instance: T) : Ref<T> {
    private val weakReference = WeakReference(instance)

    override val value: T?
        get() = weakReference.get()
}

actual object RefFactory {
    actual fun <T : Any> create(any: T): Ref<T> = RefImp(any)
}