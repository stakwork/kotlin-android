package io.matthewnelson.k_openssl_common.clazzes

import io.matthewnelson.k_openssl_common.annotations.UnencryptedDataAccess
import io.matthewnelson.k_openssl_common.extensions.toCharArray

inline class UnencryptedByteArray(@property: UnencryptedDataAccess val value: ByteArray) {

    @OptIn(UnencryptedDataAccess::class)
    fun clear() {
        value.fill('*'.toByte())
    }

    @Throws(IllegalAccessException::class)
    override fun toString(): String {
        throw IllegalAccessException("toUnencryptedString must be used.")
    }

    @OptIn(UnencryptedDataAccess::class)
    fun toUnencryptedCharArray(): UnencryptedCharArray =
        UnencryptedCharArray(value.toCharArray())

    @OptIn(UnencryptedDataAccess::class)
    fun toUnencryptedString(): UnencryptedString =
        UnencryptedString(value.toString(Charsets.UTF_8).trim())
}
