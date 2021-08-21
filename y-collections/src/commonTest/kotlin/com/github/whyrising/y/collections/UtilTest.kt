package com.github.whyrising.y.collections

import com.github.whyrising.y.collections.concretions.map.PersistentArrayMap
import com.github.whyrising.y.collections.concretions.set.PersistentHashSet.TransientHashSet
import com.github.whyrising.y.collections.concretions.set.hashSet
import com.github.whyrising.y.collections.core.get
import com.github.whyrising.y.collections.core.getFrom
import com.github.whyrising.y.collections.core.m
import com.github.whyrising.y.collections.core.v
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UtilTest {
    @Test
    fun `get(map, key)`() {
        val arrayMap = m(":a" to 5, ":b" to 6, ":c" to 3) as PersistentArrayMap
        val transientHashSet = TransientHashSet(arrayMap.asTransient())

        get(mapOf(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
        get(m(":a" to 1, ":b" to 2, ":c" to 3), ":a") shouldBe 1
        get(v(5, 6, 9, 3), 0) shouldBe 5
        get(hashSet(54, 69, 36), 54) shouldBe 54
        get(transientHashSet, ":a") shouldBe 5
        getFrom<Any, Int>(m(":a" to 15, ":b" to 74), ":a") shouldBe 15
        getFrom<Any, Int>(null, ":a").shouldBeNull()

        shouldThrowExactly<IllegalArgumentException> {
            getFrom<Any, Int>(listOf(1, 5, 3), ":a")
        }.message shouldBe "`[1, 5, 3]` is not associative."
    }

    @Test
    fun `get(map, key) should return null`() {
        val arrayMap = m(":a" to 5, ":b" to 6, ":c" to 3) as PersistentArrayMap
        val transientHashSet = TransientHashSet(arrayMap.asTransient())

        get(mapOf(":a" to 1, ":b" to 2, ":c" to 3), ":x").shouldBeNull()
        get(m(":a" to 1, ":b" to 2, ":c" to 3), ":x").shouldBeNull()
        get(v(5, 6, 9, 3), 10).shouldBeNull()
        get(hashSet(54, 69, 36), 66).shouldBeNull()
        get(transientHashSet, ":x").shouldBeNull()
        getFrom<Any, Int>(m(":a" to 15, ":b" to 74), ":x").shouldBeNull()
    }

    @Test
    fun `get(map, key) should return default`() {
        val arrayMap = m(":a" to 5, ":b" to 6, ":c" to 3) as PersistentArrayMap
        val transientHashSet = TransientHashSet(arrayMap.asTransient())

        get(mapOf(":a" to 1, ":b" to 2, ":c" to 3), ":x", -1) shouldBe -1
        get(m(":a" to 1, ":b" to 2, ":c" to 3), ":x", -1) shouldBe -1
        get(v(5, 6, 9, 3), 10, -1) shouldBe -1
        get(hashSet(54, 69, 36), 66, -1) shouldBe -1
        get(transientHashSet, ":x", -1) shouldBe -1
        getFrom<Any, Int>(m(":a" to 15, ":b" to 74), ":x", -1) shouldBe -1
    }
}
