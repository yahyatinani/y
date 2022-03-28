package com.github.whyrising.y.mocks

class User(val id: Int) {
    override fun hashCode(): Int = 18

    override fun equals(other: Any?): Boolean = when (other) {
        is User -> id == other.id
        else -> false
    }
}
