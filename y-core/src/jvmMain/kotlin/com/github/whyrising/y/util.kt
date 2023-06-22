package com.github.whyrising.y

import java.lang.ref.Reference
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentHashMap

fun <K, V> clearCache(
  rq: ReferenceQueue<Keyword>,
  cache: ConcurrentHashMap<K, Reference<V>>,
) {
  // cleanup any dead entries
  if (rq.poll() != null) {
    while (rq.poll() != null);

    for (e: Map.Entry<K, Reference<V>> in cache.entries) {
      val v = e.value
      if (v.get() == null) {
        cache.remove(e.key, v)
      }
    }
  }
}
