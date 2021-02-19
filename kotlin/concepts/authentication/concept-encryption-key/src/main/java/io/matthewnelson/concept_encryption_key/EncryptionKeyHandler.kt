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
