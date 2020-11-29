package com.github.whyrising.y

import kotlin.native.internal.GC
import kotlin.native.ref.WeakReference
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RefFactoryTest {
    @BeforeTest
    fun setUp() {
        (keywordsCache() as HashMap<Symbol, Any>).clear()

        Keyword("a")
        Keyword("b")
        Keyword("c")
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun create() {
        val symbol = Symbol("a")

        val weakReference = RefFactory.create(symbol) as WeakReference<Symbol>

        assertNotNull(weakReference)
        assertEquals(symbol, weakReference.value)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun valueOf() {
        val symbol = Symbol("a")

        val value = RefFactory.valueOf<Symbol>(RefFactory.create(symbol))

        assertEquals(symbol, value)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun assertWeakRefIsCollected() {
        val cache = keywordsCache() as HashMap<Symbol, WeakReference<Keyword>>

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
