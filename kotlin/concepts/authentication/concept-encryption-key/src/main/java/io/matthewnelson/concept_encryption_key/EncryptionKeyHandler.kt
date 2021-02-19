package io.matthewnelson.concept_encryption_key

import io.matthewnelson.k_openssl_common.clazzes.HashIterations
import io.matthewnelson.k_openssl_common.clazzes.Password

abstract class EncryptionKeyHandler {

    abstract fun generateEncryptionKey(): EncryptionKey

    @Throws(EncryptionKeyException::class)
    fun storeCopyOfEncryptionKey(key: CharArray): EncryptionKey =
        validateEncryptionKey(key)

    @Throws(EncryptionKeyException::class)
    protected abstract fun validateEncryptionKey(key: CharArray): EncryptionKey

    /**
     * Call from [validateEncryptionKey] if everything checks out.
     * */
    protected fun copyAndStoreKey(key: CharArray): EncryptionKey =
        EncryptionKey.instantiate(Password(key.copyOf()))

    /**
     * The [HashIterations] used to encrypt/decrypt things when using the
     * [EncryptionKey], not the [HashIterations] used to encrypt/decrypt the
     * actual key that gets set as a constructor argument for
     * [io.matthewnelson.feature_authentication_core.components.AuthenticationManagerImpl]
     * */
    abstract fun getHashIterations(key: EncryptionKey): HashIterations
}
