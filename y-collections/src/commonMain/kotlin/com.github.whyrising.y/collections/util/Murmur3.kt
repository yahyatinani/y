/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express
 * or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

/*
 * MurmurHash3 was written by Austin Appleby, and is placed in the public
 * domain. The author hereby disclaims copyright to this source code.
 */

/*
 * Source:
 * http://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp
 * (Modified to adapt to Guava coding conventions and to use the HashFunction
 * interface)
 */

/*
 * Modified to remove stuff Clojure doesn't need, placed under clojure.lang
 * namespace, all fns made static, added hashOrdered/Unordered
 */

/*
 * This code was taken from Clojure's source code as is and modified to work in
 * Kotlin.
 */

package com.github.whyrising.y.collections.util

/**
 * See http://smhasher.googlecode.com/svn/trunk/MurmurHash3.cpp
 * MurmurHash3_x86_32
 *
 * @author Austin Appleby
 * @author Dimitris Andreou
 * @author Kurt Alfred Kluever
 */
object Murmur3 {
    private const val seed: Int = 0
    private const val C1 = -0x3361d2af
    private const val C2 = 0x1b873593

    private fun fmix(h1: Int, length: Int): Int {
        var temp = h1 xor length
        temp = temp xor (temp ushr 16)
        temp *= -0x7a143595
        temp = temp xor (temp ushr 13)
        temp *= -0x3d4d51cb
        temp = temp xor (temp ushr 16)

        return temp
    }

    private fun mixK1(k1: Int): Int {
        var temp = k1 * C1
        temp = temp.rotateLeft(15)
        temp *= C2

        return temp
    }

    private fun mixH1(h1: Int, k1: Int): Int {
        var temp = h1 xor k1
        temp = temp.rotateLeft(13)
        temp = temp * 5 + -0x19ab949c

        return temp
    }

    fun hashInt(x: Int): Int {
        if (x == 0) return 0

        val k1 = mixK1(x)
        val h1 = mixH1(seed, k1)

        return fmix(h1, 4)
    }

    fun hashLong(x: Long): Int {
        if (x == 0L) return 0

        val low = x.toInt()
        val high = (x ushr 32).toInt()

        var k1 = mixK1(low)
        var h1 = mixH1(seed, k1)

        k1 = mixK1(high)
        h1 = mixH1(h1, k1)

        return fmix(h1, 8)
    }

    fun hashUnencodedChars(input: CharSequence): Int {
        var h1 = seed

        // step through the CharSequence 2 chars at a time
        var i = 1
        while (i < input.length) {
            var k1 = input[i - 1].code or (input[i].code shl 16)
            k1 = mixK1(k1)
            h1 = mixH1(h1, k1)
            i += 2
        }

        // deal with any remaining characters
        if (input.length and 1 == 1) {
            var k1 = input[input.length - 1].code
            k1 = mixK1(k1)
            h1 = h1 xor k1
        }

        return fmix(h1, 2 * input.length)
    }

    fun mixCollHash(hash: Int, count: Int): Int {
        var h1 = seed
        val k1 = mixK1(hash)
        h1 = mixH1(h1, k1)

        return fmix(h1, count)
    }

    fun <E> hashOrdered(xs: Iterable<E>): Int {
        var n = 0
        var hash = 1
        for (x in xs) {
            hash = 31 * hash + hasheq(x)
            ++n
        }

        return mixCollHash(hash, n)
    }
    
    fun <E> hashUnordered(xs: Iterable<E>): Int {
        var hash = 0
        var n = 0
        for (x in xs) {
            hash += hasheq(x)
            ++n
        }

        return mixCollHash(hash, n)
    }
}
