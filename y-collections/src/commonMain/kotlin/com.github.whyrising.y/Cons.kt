package com.github.whyrising.y

import com.github.whyrising.y.PersistentList.Empty
import com.github.whyrising.y.core.ConstantCount
import com.github.whyrising.y.seq.ISeq

class Cons<out E>(first: E, rest: ISeq<E>) : ASeq<E>() {

    private var _first: E = first
    private var _rest: ISeq<E> = rest

    override fun first(): E = _first

    override fun rest(): ISeq<E> = _rest

    // Move to Util when needed
    private fun count(): Int {
        if (_rest is ConstantCount)
            return _rest.count

        var size = 0
        var rest = _rest
        while (rest !is Empty) {
            if (rest is ConstantCount)
                return size + rest.count

            rest = rest.rest()
            size++
        }

        return size
    }

    override val count: Int
        get() = 1 + count()
}
