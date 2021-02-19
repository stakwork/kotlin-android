package io.matthewnelson.k_openssl_common.clazzes

import io.matthewnelson.k_openssl_common.annotations.UnencryptedDataAccess
import io.matthewnelson.k_openssl_common.extensions.toByteArray

inline class UnencryptedCharArray(@property: UnencryptedDataAccess val value: CharArray) {

    @OptIn(UnencryptedDataAccess::class)
    fun clear() {
        value.fill('*')
    }

    @Throws(IllegalAccessException::class)
    override fun toString(): String {
        throw IllegalAccessException("toUnencryptedString must be used.")
    }

    @OptIn(UnencryptedDataAccess::class)
    fun toUnencryptedString(): UnencryptedString =
        UnencryptedString(value.joinToString(""))

    @OptIn(UnencryptedDataAccess::class)
    fun toUnencryptedByteArray(): UnencryptedByteArray =
        UnencryptedByteArray(value.toByteArray())
}
