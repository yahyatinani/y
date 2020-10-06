package com.github.whyrising.y

interface Ops {

    fun combine(y: Ops): Ops

    fun u(x: LongOps): Ops

    fun u(x: DoubleOps): Ops

    fun equiv(x: Number, y: Number): Boolean
}

object LongOps : Ops {

    override fun combine(y: Ops): Ops = y.u(this)

    override fun u(x: LongOps): Ops = this

    override fun u(x: DoubleOps): Ops = DoubleOps

    override fun equiv(x: Number, y: Number): Boolean =
        x.toLong() == y.toLong()
}

object DoubleOps : Ops {

    override fun combine(y: Ops): Ops = y.u(this)

    override fun u(x: LongOps): Ops = this

    override fun u(x: DoubleOps): Ops = this

    override fun equiv(x: Number, y: Number): Boolean =
        x.toDouble() == y.toDouble()
}
