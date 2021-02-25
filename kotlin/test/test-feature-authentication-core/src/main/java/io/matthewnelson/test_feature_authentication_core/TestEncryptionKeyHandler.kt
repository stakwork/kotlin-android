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
package io.matthewnelson.test_feature_authentication_core

import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.concept_encryption_key.EncryptionKeyException
import io.matthewnelson.concept_encryption_key.EncryptionKeyHandler
import io.matthewnelson.k_openssl_common.clazzes.HashIterations

/**
 * Extend and implement your own overrides if desired.
 * */
open class TestEncryptionKeyHandler: EncryptionKeyHandler() {

    companion object {
        const val TEST_ENCRYPTION_KEY_STRING = "TEST_ENCRYPTION_KEY_STRING"
    }

    override suspend fun generateEncryptionKey(): EncryptionKey {
        return copyAndStoreKey(TEST_ENCRYPTION_KEY_STRING.toCharArray())
    }

    override fun validateEncryptionKey(key: CharArray): EncryptionKey {
        val keyString = key.joinToString("")
        if (keyString != TEST_ENCRYPTION_KEY_STRING) {
            throw EncryptionKeyException("EncryptionKey: $keyString != $TEST_ENCRYPTION_KEY_STRING")
        }

        return copyAndStoreKey(key)
    }

    override fun getHashIterations(key: EncryptionKey): HashIterations {
        return HashIterations(1)
    }
}