import com.github.whyrising.y.Keyword
import com.github.whyrising.y.Symbol
import com.github.whyrising.y.k
import com.github.whyrising.y.keywordsCache
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordTest {
    @BeforeTest
    fun setUp() {
        (keywordsCache() as HashMap<Symbol, Any>).clear()
    }

    @ExperimentalStdlibApi
    @Test
    fun ctor() {
        val key = Keyword("a")

        assertEquals(Symbol("a"), key.symbol)
        assertEquals(Symbol("a").hasheq() + -0x61c88647, key.hashEq)
    }

    @Test
    fun `toString() should return print property`() {
        assertEquals(key.toString(), ":a")
        assertEquals(key.print, ":a")

        val k = Keyword("b")
        assertEquals(k.toString(), ":b")
        assertEquals(k.print, ":b")
    }

    companion object {
        val key = k("a")
    }
}