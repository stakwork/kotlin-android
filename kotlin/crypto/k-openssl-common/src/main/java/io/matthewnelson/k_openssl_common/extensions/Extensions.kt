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
package io.matthewnelson.k_openssl_common.extensions

import java.nio.ByteBuffer
import java.nio.CharBuffer

/** securely converts a ByteArray to a CharArray */
@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toCharArray(): CharArray =
    this.copyOf().let { copyByteArray ->
        ByteBuffer.wrap(copyByteArray).let { byteBuffer ->
            charset("UTF-8").newDecoder().decode(byteBuffer).let { charBuffer ->
                charBuffer.array().copyOf(charBuffer.limit()).let { charArray ->
                    byteBuffer.array().fill('*'.toByte())
                    charBuffer.array().fill('*')
                    charArray
                }
            }
        }
    }

/** securely converts a CharArray to a ByteArray */
@Suppress("NOTHING_TO_INLINE")
inline fun CharArray.toByteArray(): ByteArray =
    this.copyOf().let { copyCharArray ->
        CharBuffer.wrap(copyCharArray).let { charBuffer ->
            charset("UTF-8").newEncoder().encode(charBuffer).let { byteBuffer ->
                byteBuffer.array().copyOf(byteBuffer.limit()).let { byteArray ->
                    charBuffer.array().fill('*')
                    byteBuffer.array().fill('*'.toByte())
                    byteArray
                }
            }
        }
    }

@Suppress("NOTHING_TO_INLINE")
inline fun String.encodeToByteArray(): ByteArray =
    this.toByteArray(charset("UTF-8"))

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toHex(): String =
    StringBuilder(this.size * 2).let { hex ->
        for (b in this) {
            hex.append(String.format("%02x", b, 0xFF))
        }
        hex.toString()
    }