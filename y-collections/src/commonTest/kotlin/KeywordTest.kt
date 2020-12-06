import com.github.whyrising.y.Keyword
import com.github.whyrising.y.k
import kotlin.test.Test
import kotlin.test.assertEquals

class KeywordTest {
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