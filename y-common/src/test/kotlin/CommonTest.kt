import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CommonTest : FunSpec({
    test("Should pass!") {
        Common.nothing() shouldBe true
    }
})
