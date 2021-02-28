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
package io.matthewnelson.feature_authentication_core.model

import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.concept_encryption_key.EncryptionKeyHandler
import io.matthewnelson.k_openssl.KOpenSSL
import io.matthewnelson.k_openssl_common.annotations.UnencryptedDataAccess
import io.matthewnelson.k_openssl_common.clazzes.*

internal class Credentials private constructor(
    private val encryptedEncryptionKey: EncryptedString,
    private val encryptionKeyTestString: EncryptedString
) {

    companion object {
        const val ENCRYPTION_KEY_TEST_STRING_VALUE = "There will only ever be 21 million..."
        private const val DELIMITER = "|-SAFU-|"

        @JvmSynthetic
        fun instantiate(
            encryptedEncryptionKey: EncryptedString,
            encryptionKeyTestString: EncryptedString
        ): Credentials =
            Credentials(
                encryptedEncryptionKey,
                encryptionKeyTestString
            )

        @JvmSynthetic
        @Throws(IllegalArgumentException::class)
        fun fromString(string: String): Credentials =
            string.split(DELIMITER).let { list ->
                if (
                    list.size != 2 ||
                    !KOpenSSL.isSalted(list[0]) ||
                    !KOpenSSL.isSalted(list[1])
                ) {
                    throw IllegalArgumentException(
                        "String value did not meet requirements for creating Credentials"
                    )
                }

                return Credentials(
                    EncryptedString(list[0]),
                    EncryptedString(list[1])
                )
            }
    }

    @JvmSynthetic
    @Throws(AuthenticationException::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun decryptEncryptionKey(
        dispatchers: CoroutineDispatchers,
        encryptionKeyHashIterations: HashIterations,
        encryptionKeyHandler: EncryptionKeyHandler,
        kOpenSSL: KOpenSSL,
        userInput: UserInputWriter
    ): EncryptionKey {
        val password = Password(userInput.toCharArray())
        try {
            val unencryptedByteArray = kOpenSSL.decrypt(
                encryptionKeyHashIterations,
                password,
                encryptedEncryptionKey,
                dispatchers.default
            )

            val unencryptedCharArray = unencryptedByteArray.toUnencryptedCharArray()
            unencryptedByteArray.clear()

            @OptIn(UnencryptedDataAccess::class)
            return try {
                encryptionKeyHandler.storeCopyOfEncryptionKey(unencryptedCharArray.value)
            } finally {
                unencryptedCharArray.clear()
            }
        } catch (e: Exception) {
            throw AuthenticationException(
                AuthenticateFlowResponse.Error.FailedToDecryptEncryptionKey
            )
        } finally {
            password.clear()
        }
    }

    @JvmSynthetic
    override fun toString(): String {
        StringBuilder().let { sb ->
            sb.append(encryptedEncryptionKey.value)
            sb.append(DELIMITER)
            sb.append(encryptionKeyTestString.value)
            return sb.toString()
        }
    }

    @JvmSynthetic
    @OptIn(UnencryptedDataAccess::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun validateTestString(
        dispatchers: CoroutineDispatchers,
        encryptionKey: EncryptionKey,
        encryptionKeyHandler: EncryptionKeyHandler,
        kOpenSSL: KOpenSSL
    ): Boolean {
        return try {
            kOpenSSL.decrypt(
                encryptionKey.password,
                encryptionKeyHandler.getTestStringEncryptHashIterations(encryptionKey),
                encryptionKeyTestString,
                dispatchers.default
            ).let { result ->
                result.value == ENCRYPTION_KEY_TEST_STRING_VALUE
            }
        } catch (e: Exception) {
            false
        }
    }
}
