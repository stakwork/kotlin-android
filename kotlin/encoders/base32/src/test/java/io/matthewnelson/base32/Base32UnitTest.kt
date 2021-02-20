/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
package io.matthewnelson.base32

import com.google.common.io.BaseEncoding
import org.junit.Assert
import org.junit.Test
import java.io.CharArrayWriter
import java.security.SecureRandom

/**
 * Tests decode/encode output against Guava
 * */
class Base32UnitTest {

    private val sRandom: SecureRandom = SecureRandom()
    private val testDepth: Int = 1_000

    @Test
    fun `Okio base32 _decode_ matches Guava output`() {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()

        for (i in 0 until testDepth) {

            val charArrayWriter = CharArrayWriter(i)

            // Build character array of randomly chosen base32 characters that varies in size
            repeat(i) {
                charArrayWriter.append(base32Chars[sRandom.nextInt(base32Chars.size)])
            }

            charArrayWriter.toString().let { randomEncoding ->

                // Base32 (A-Z and 2-7)
                randomEncoding.decodeBase32ToArray(Base32.Default)?.let { okioDecoded ->

                    val guavaDecoded = BaseEncoding.base32().decode(randomEncoding)
                    Assert.assertEquals(okioDecoded.size, guavaDecoded.size)
                    Assert.assertEquals(
                        okioDecoded.toString(charset("UTF-8")),
                        guavaDecoded.toString(charset("UTF-8")))

                } ?: let {
                    // Okio base32 returned null, so Guava BaseEncoding should
                    // throw an exception for the given string value 'randomEncoding'
                    try {
                        BaseEncoding.base32().decode(randomEncoding)

                        // Fail test if IllegalArgumentException is not thrown
                        Assert.fail()
                    } catch (e: IllegalArgumentException) {
                        // Expected exception. Pass
                    }
                }
            }
        }
    }

    @Test
    fun `Okio base32hex _decode_ matches Guava output`() {
        val base32Chars = "0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray()

        for (i in 0 until testDepth) {

            val charArrayWriter = CharArrayWriter(i)

            // Build character array of randomly chosen base32 characters that varies in size
            repeat(i) {
                charArrayWriter.append(base32Chars[sRandom.nextInt(base32Chars.size)])
            }

            charArrayWriter.toString().let { randomEncoding ->

                // Base32Hex (0-9 and A-V)
                randomEncoding.decodeBase32ToArray(Base32.Hex)?.let { okioDecoded ->

                    val guavaDecoded = BaseEncoding.base32Hex().decode(randomEncoding)
                    Assert.assertEquals(okioDecoded.size, guavaDecoded.size)
                    Assert.assertEquals(
                        okioDecoded.toString(charset("UTF-8")),
                        guavaDecoded.toString(charset("UTF-8"))
                    )

                } ?: let {
                    // Okio base32 returned null, so Guava BaseEncoding should
                    // throw an exception for the given string value 'randomEncoding'
                    try {
                        BaseEncoding.base32Hex().decode(randomEncoding)

                        // Fail test if IllegalArgumentException is not thrown
                        Assert.fail()
                    } catch (e: IllegalArgumentException) {
                        // Expected exception. Pass
                    }
                }
            }
        }
    }

    @Test
    fun `Okio base32 _encode_ matches Guava output`() {
        for (i in 0 until testDepth) {
            val randomByteArray = ByteArray(i)

            // Build ByteArray of randomly chosen bytes
            for (j in 0 until i) {
                randomByteArray[j] = sRandom.nextInt().toByte()
            }

            randomByteArray.encodeBase32(Base32.Default).let { okioEncoded ->
                BaseEncoding.base32().encode(randomByteArray).let { guavaEncoded ->
                    Assert.assertEquals(okioEncoded, guavaEncoded)
                }
            }
        }
    }

    @Test
    fun `Okio base32hex _encode_ matches Guava output`() {
        for (i in 0 until testDepth) {
            val randomByteArray = ByteArray(i)

            // Build ByteArray of randomly chosen bytes
            for (j in 0 until i) {
                randomByteArray[j] = sRandom.nextInt().toByte()
            }

            randomByteArray.encodeBase32(Base32.Hex).let { okioEncoded ->
                BaseEncoding.base32Hex().encode(randomByteArray).let { guavaEncoded ->
                    Assert.assertEquals(okioEncoded, guavaEncoded)
                }
            }
        }
    }
}