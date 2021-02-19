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
