package com.github.whyrising.y

import kotlin.native.internal.GC
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RefFactoryTest {
    @BeforeTest
    fun setUp() {
        (keywordsCache() as HashMap<Symbol, Ref<Keyword>>).clear()

        Keyword("a")
        Keyword("b")
        Keyword("c")
    }

    @Test
    fun create() {
        val value = Symbol("a")

        val ref = RefFactory.create(value) as RefImpl<Symbol>
        val weakReference = ref.weakReference

        assertNotNull(weakReference)
        assertEquals(value, weakReference.value)
        assertEquals(value, ref.value)
    }

    @Test
    fun assertWeakRefIsCollected() {
        val cache = keywordsCache()

        GC.collect()

        assertEquals(3, cache.size)
        assertNull(cache[Symbol("a")]?.value)
        assertNull(cache[Symbol("b")]?.value)
        assertNull(cache[Symbol("c")]?.value)

        Keyword("a")
        Keyword("b")
        Keyword("c")

        assertNotNull(cache[Symbol("a")]?.value)
        assertNotNull(cache[Symbol("b")]?.value)
        assertNotNull(cache[Symbol("c")]?.value)
    }
}
