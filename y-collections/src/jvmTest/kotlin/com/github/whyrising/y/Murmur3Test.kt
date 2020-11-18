package com.github.whyrising.y

import com.github.whyrising.y.util.Murmur3
import com.github.whyrising.y.util.hasheq
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.ints.shouldBeExactly

@ExperimentalStdlibApi
class Murmur3Test : FreeSpec({
    "Murmur3.hashInt(x)" {
        Murmur3.hashInt(0) shouldBeExactly 0
        Murmur3.hashInt(1) shouldBeExactly -68075478
        Murmur3.hashInt(756) shouldBeExactly 345687756
        Murmur3.hashInt(15416513) shouldBeExactly -2099161683
    }

    "Murmur3.hashLong(x)" {
        Murmur3.hashLong(0) shouldBeExactly 0
        Murmur3.hashLong(1) shouldBeExactly 1392991556
        Murmur3.hashLong(756) shouldBeExactly 952128707
        Murmur3.hashLong(15416513) shouldBeExactly 902933594
    }

    "Murmur3.hashUnencodedChars(x)" {
        Murmur3.hashUnencodedChars("0") shouldBeExactly 384918240
        Murmur3.hashUnencodedChars("1") shouldBeExactly -126235597
        Murmur3.hashUnencodedChars("756") shouldBeExactly -214656108
        Murmur3.hashUnencodedChars("15416513") shouldBeExactly -643974253
    }

    "Murmur3.mixCollHash(x)" {
        Murmur3.mixCollHash(0, 0) shouldBeExactly -15128758
        Murmur3.mixCollHash(1, 1) shouldBeExactly 1978132887
        Murmur3.mixCollHash(756, 56) shouldBeExactly 818062189
        Murmur3.mixCollHash(15416513, 1456) shouldBeExactly -158721406
    }

    "Murmur3.hashOrdered(x)" {
        val l1 = l("Mango", 1, 32569885145, 12.toShort(), -0.0f, true)
        val l2 = l("Mango", "Apple")
        val l3 = l("Mango", "Apple", "Banana")
        val l4 = l("Mango", "Apple", "Banana", "Grapes")

        Murmur3.hashOrdered(l1) shouldBeExactly 740609528
        Murmur3.hashOrdered(l2) shouldBeExactly -1419580492
        Murmur3.hashOrdered(l3) shouldBeExactly -1873437059
        Murmur3.hashOrdered(l4) shouldBeExactly 1554645307
    }

    "Murmur3.hashUnordered(x)" {
        val arrayMap: PersistentArrayMap<Any, Any> =
            PersistentArrayMap("a" to "b", true to "false")
        var expectedHash = 0
        for (x in arrayMap) expectedHash += hasheq(x)
        expectedHash = Murmur3.mixCollHash(expectedHash, arrayMap.size)

        Murmur3.hashUnordered(arrayMap) shouldBeExactly expectedHash
    }
})
