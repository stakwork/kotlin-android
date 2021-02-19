package io.matthewnelson.concept_encryption_key

import io.matthewnelson.k_openssl_common.clazzes.Password

class EncryptionKey private constructor(val password: Password) {
    companion object {
        @JvmSynthetic
        internal fun instantiate(password: Password): EncryptionKey =
            EncryptionKey(password)
    }
}