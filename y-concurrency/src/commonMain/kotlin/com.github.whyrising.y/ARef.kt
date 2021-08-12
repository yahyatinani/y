package com.github.whyrising.y

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic

abstract class ARef<T> : IRef<T> {
    private val _validator: AtomicRef<((T) -> Boolean)?> = atomic(null)

    private fun validate(vf: ((T) -> Boolean)?, value: T) {
        if (vf == null) return

        fun invalidReferenceState(cause: Throwable? = null) =
            IllegalStateException("Invalid reference state", cause)

        val isValid = try {
            vf(value)
        } catch (e: Exception) {
            throw invalidReferenceState(e)
        }

        if (!isValid)
            throw invalidReferenceState()
    }

    fun validate(value: T) {
        validate(_validator.value, value)
    }

    override var validator: ((T) -> Boolean)?
        get() = _validator.value
        set(vf) {
            validate(vf, deref())
            _validator.value = vf
        }
}
