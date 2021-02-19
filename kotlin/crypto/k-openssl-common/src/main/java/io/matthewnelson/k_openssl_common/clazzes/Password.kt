package io.matthewnelson.k_openssl_common.clazzes

import io.matthewnelson.k_openssl_common.annotations.RawPasswordAccess

inline class Password(@property: RawPasswordAccess val value: CharArray) {

    @OptIn(RawPasswordAccess::class)
    fun clear() {
        value.fill('*')
    }

    @Throws(IllegalAccessException::class)
    override fun toString(): String {
        throw IllegalAccessException(
            "Strings are unable to be modified prior to being garbage collected"
        )
    }
}
