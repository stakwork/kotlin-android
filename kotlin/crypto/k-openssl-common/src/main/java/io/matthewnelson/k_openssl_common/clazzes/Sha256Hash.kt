package io.matthewnelson.k_openssl_common.clazzes

import io.matthewnelson.k_openssl_common.extensions.encodeToByteArray
import io.matthewnelson.k_openssl_common.extensions.toHex
import java.security.MessageDigest

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toSha256Hash(): Sha256Hash {
    MessageDigest.getInstance("SHA-256").let { digest ->
        digest.reset()
        digest.update(this, 0, this.size)
        return Sha256Hash(digest.digest().toHex())
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun String.toSha256Hash(): Sha256Hash =
    this.encodeToByteArray().toSha256Hash()

class Sha256Hash(val value: String) {

    // TODO: Kotlin 1.4.30 update will allow inline classes to
    //  have init blocks
    init {
        require(isValid(value)) {
            "$value is not a valid Sha256 hash"
        }
    }

    companion object {
        fun isValid(sha256Hash: String): Boolean =
            sha256Hash.matches("[a-f0-9]{64}".toRegex())

        fun isValid(sha256Hash: ByteArray): Boolean =
            isValid(sha256Hash.toHex())
    }

    override fun toString(): String {
        return "Sha256Hash(value=$value)"
    }
}