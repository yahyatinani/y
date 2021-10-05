package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.ASeq
import com.github.whyrising.y.collections.concretions.list.PersistentList
import com.github.whyrising.y.collections.seq.ISeq
import com.github.whyrising.y.collections.seq.IndexedSeq

class StringSeq internal constructor(
    val s: CharSequence,
    val i: Int
) : ASeq<Char>(), IndexedSeq {
    override fun first(): Char = s[i]

    override fun next(): ISeq<Char>? = when {
        i + 1 < s.length -> StringSeq(s, i + 1)
        else -> null
    }

    override val count: Int
        get() = s.length - i

    override val index: Int
        get() = i

    companion object {
        operator fun invoke(s: CharSequence): ISeq<Char> = when {
            s.isEmpty() -> PersistentList.Empty
            else -> StringSeq(s, 0)
        }
    }
}
