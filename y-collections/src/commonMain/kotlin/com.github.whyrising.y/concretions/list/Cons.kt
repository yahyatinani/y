package com.github.whyrising.y.concretions.list

import com.github.whyrising.y.concretions.list.PersistentList.Empty
import com.github.whyrising.y.core.InstaCount
import com.github.whyrising.y.seq.ISeq

class Cons<out E>(
    private val _first: E,
    private val _rest: ISeq<E>
) : ASeq<E>() {

    override fun first(): E = _first

    override fun rest(): ISeq<E> = _rest

    // Move to Util when needed
    private fun count(): Int {
        when (_rest) {
            is InstaCount -> return _rest.count
            else -> {
                var size = 0
                var rest = _rest
                while (rest !is Empty) {
                    if (rest is InstaCount) return size + rest.count

                    rest = rest.rest()
                    size++
                }
                return size
            }
        }
    }

    override val count: Int
        get() = 1 + count()
}
