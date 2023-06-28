package io.github.yahyatinani.y.core.collections

interface IPersistentVector<out E> :
  List<E>,
  Associative<Int, E>,
  IPersistentCollection<E>,
  IPersistentStack<E>,
  Indexed<E>,
  Sequential {

  fun length(): Int

  fun assocN(index: Int, value: @UnsafeVariance E): IPersistentVector<E>

  override fun conj(e: @UnsafeVariance E): IPersistentVector<E>

  fun subvec(start: Int, end: Int): IPersistentVector<E>
}
