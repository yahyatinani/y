package io.github.yahyatinani.y.core.collections

interface Indexed<out E> : InstaCount {
  fun nth(index: Int): E

  fun nth(index: Int, default: @UnsafeVariance E): E
}
