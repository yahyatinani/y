package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.list.PersistentList.Empty
import com.github.whyrising.y.collections.concretions.vector.PersistentVector
import com.github.whyrising.y.collections.concretions.vector.PersistentVector.EmptyVector
import com.github.whyrising.y.collections.seq.ISeq

class PersistentQueue<out E> private constructor(
    val count: Int,
    val front: ISeq<E>,
    val back: PersistentVector<E>
) {
    companion object {
        operator fun <E> invoke(): PersistentQueue<E> =
            PersistentQueue<E>(0, Empty, EmptyVector)
    }
}
