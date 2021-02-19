package io.matthewnelson.feature_authentication_core.components

import app.cash.exhaustive.Exhaustive
import io.matthewnelson.concept_authentication.AuthenticationRequest
import io.matthewnelson.concept_authentication.AuthenticationResponse
import io.matthewnelson.feature_authentication_core.data.PersistentStorage
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse
import io.matthewnelson.feature_authentication_core.model.AuthenticationException
import io.matthewnelson.feature_authentication_core.model.AuthenticationState
import io.matthewnelson.feature_authentication_core.model.Credentials
import io.matthewnelson.feature_authentication_core.model.PinEntry
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.concept_encryption_key.EncryptionKeyHandler
import io.matthewnelson.k_openssl.KOpenSSL
import io.matthewnelson.k_openssl.algos.AES256CBC_PBKDF2_HMAC_SHA256
import io.matthewnelson.k_openssl_common.annotations.RawPasswordAccess
import io.matthewnelson.k_openssl_common.clazzes.*
import io.matthewnelson.k_openssl_common.extensions.toByteArray
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

/**
 * TODO: Really need to implement a "login with encryption key" response to better handle
 *  errors.
 * */
internal class AuthenticationProcessor<T: AuthenticationManagerInitializer> private constructor(
    private val authenticationManagerImpl: AuthenticationManagerImpl<T>,
    private val dispatchers: CoroutineDispatchers,
    private val encryptionKeyHashIterations: HashIterations,
    val encryptionKeyHandler: EncryptionKeyHandler,
    private val persistentStorage: PersistentStorage
    // TODO: WrongPinLockout
) {

    companion object {
        @JvmSynthetic
        fun <T: AuthenticationManagerInitializer> instantiate(
            authenticationManagerImpl: AuthenticationManagerImpl<T>,
            dispatchers: CoroutineDispatchers,
            encryptionKeyHashIterations: HashIterations,
            encryptionKeyHandler: EncryptionKeyHandler,
            persistentStorage: PersistentStorage
        ): AuthenticationProcessor<T> =
            AuthenticationProcessor(
                authenticationManagerImpl,
                dispatchers,
                encryptionKeyHashIterations,
                encryptionKeyHandler,
                persistentStorage
            )
    }

    @JvmSynthetic
    fun initializeWrongPinLockout(value: T) {
//        if (!authenticationManagerImpl.isInitialized) {
            // TODO: Implement
//        }
    }

    ////////////////////
    /// Authenticate ///
    ////////////////////
    @JvmSynthetic
    fun authenticate(
        pinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> = flow {
        emit(AuthenticateFlowResponse.Notify.DecryptingEncryptionKey)
        validatePinEntry(AES256CBC_PBKDF2_HMAC_SHA256(), pinEntry).let { response ->
            @Exhaustive
            when (response) {
                is PinValidationResponse.PinEntryIsValid -> {
                    // TODO: Clear WrongPinLockout
                    emitAll(
                        processValidPinEntryResponse(
                            response.encryptionKey,
                            pinEntry,
                            requests
                        )
                    )
                }
                is PinValidationResponse.PinEntryIsNotValid -> {
                    // TODO: WrongPinLockout
                    emit(AuthenticateFlowResponse.WrongPin.instantiate(2))
                }
                is PinValidationResponse.NoCredentials -> {
                    emit(
                        AuthenticateFlowResponse.ConfirmPinEntryToSetForFirstTime
                            .instantiate(pinEntry.clone())
                    )
                }
                is PinValidationResponse.CredentialsFromStringError -> {
//                    response.credentialsString
                    // TODO: Persisted String value was malformed. Add logic to
                    //  handle it.
                }
            }
        }
    }

    private suspend fun validatePinEntry(
        kOpenSSL: KOpenSSL,
        pinEntry: PinEntry
    ): PinValidationResponse =
        persistentStorage.retrieveCredentials()?.let { credsString ->
            val creds = try {
                Credentials.fromString(credsString)
            } catch (e: IllegalArgumentException) {
                return PinValidationResponse.CredentialsFromStringError(credsString)
            }

            val key: EncryptionKey = try {
                creds.decryptEncryptionKey(
                    dispatchers,
                    encryptionKeyHashIterations,
                    encryptionKeyHandler,
                    kOpenSSL,
                    pinEntry
                )
            } catch (e: AuthenticationException) {
                return PinValidationResponse.PinEntryIsNotValid
            }

            return if (creds.validateTestString(dispatchers, key, encryptionKeyHandler, kOpenSSL)) {
                PinValidationResponse.PinEntryIsValid(key)
            } else {
                PinValidationResponse.PinEntryIsNotValid
            }
        } ?: PinValidationResponse.NoCredentials

    private sealed class PinValidationResponse {
        class PinEntryIsValid(val encryptionKey: EncryptionKey): PinValidationResponse()
        object PinEntryIsNotValid: PinValidationResponse()
        object NoCredentials: PinValidationResponse()
        class CredentialsFromStringError(val credentialsString: String): PinValidationResponse()
    }

    /////////////////
    /// Reset Pin ///
    /////////////////
    @JvmSynthetic
    fun resetPin(
        resetPin: AuthenticateFlowResponse.ConfirmNewPinEntryToReset,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> = flow {
        emit(AuthenticateFlowResponse.Notify.EncryptingEncryptionKeyWithNewPin)

        try {
            val kOpenSSL = AES256CBC_PBKDF2_HMAC_SHA256()
            val key: EncryptionKey = authenticationManagerImpl
                .getEncryptionKeyCopy() ?: persistentStorage.retrieveCredentials()
                ?.let { credsString ->
                    val creds = try {
                        Credentials.fromString(credsString)
                    } catch (e: IllegalArgumentException) {
                        // TODO: Persisted String value was malformed. Add logic to
                        //  handle it.
                        throw AuthenticationException(
                            AuthenticateFlowResponse.Error.FailedToDecryptEncryptionKey
                        )
                    }

                    creds.decryptEncryptionKey(
                        dispatchers,
                        encryptionKeyHashIterations,
                        encryptionKeyHandler,
                        kOpenSSL,
                        resetPin.getCurrentValidPinEntry()
                    )

                } ?: throw AuthenticationException(
                // TODO: Rename to Credentials from persistent storage
                AuthenticateFlowResponse.Error.ResetPin.CredentialsFromPrefsReturnedNull
            )

            val encryptedEncryptionKey = encryptEncryptionKey(
                key,
                resetPin.getNewPinEntry() ?: let {
                    key.password.clear()
                    throw AuthenticationException(
                        AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasNull
                    )
                },
                kOpenSSL
            )
            val encryptedTestString = encryptTestString(key, kOpenSSL)

            val creds = Credentials.instantiate(
                encryptedEncryptionKey,
                encryptedTestString
            )

            if (resetPin.newPinEntryHasBeenCleared) {
                throw AuthenticationException(
                    AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasCleared
                )
            }

            persistentStorage.saveCredentials(creds)

            resetPin.onCompletion()
            emitAll(
                processValidPinEntryResponse(
                    key,
                    resetPin.getNewPinEntry() ?: throw AuthenticationException(
                        AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasNull
                    ),
                    requests
                )
            )
        } catch (e: AuthenticationException) {
            emit(e.flowResponseError)
        } catch (e: Exception) {
            emit(AuthenticateFlowResponse.Error.Unclassified(e))
        }
    }

    //////////////////////////
    /// Set Pin First Time ///
    //////////////////////////
    @JvmSynthetic
    fun setPinFirstTime(
        setPinFirstTime: AuthenticateFlowResponse.ConfirmPinEntryToSetForFirstTime,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> = flow {
        try {
            emit(AuthenticateFlowResponse.Notify.GeneratingAndEncryptingEncryptionKey)

            val newKey = encryptionKeyHandler.generateEncryptionKey()
            val kOpenssl = AES256CBC_PBKDF2_HMAC_SHA256()
            val encryptedEncryptionKey = encryptEncryptionKey(
                newKey,
                setPinFirstTime.getInitialPinEntry(),
                kOpenssl
            )
            val encryptedTestString = encryptTestString(newKey, kOpenssl)

            if (setPinFirstTime.hasBeenCleared) {
                throw AuthenticationException(AuthenticateFlowResponse.Error.SetPinFirstTime.NewPinEntryWasCleared)
            }

            val creds = Credentials.instantiate(
                encryptedEncryptionKey,
                encryptedTestString
            )

            persistentStorage.saveCredentials(creds)

            emitAll(
                processValidPinEntryResponse(newKey, setPinFirstTime.getInitialPinEntry(), requests)
            )
        } catch (e: AuthenticationException) {
            emit(e.flowResponseError)
        } catch (e: Exception) {
            emit(AuthenticateFlowResponse.Error.Unclassified(e))
        }
    }

    private suspend fun encryptEncryptionKey(
        key: EncryptionKey,
        pinEntry: PinEntry,
        kOpenSSL: KOpenSSL
    ): EncryptedString {
        Password(pinEntry.getPinWriter().toCharArray()).let { password ->

            @OptIn(RawPasswordAccess::class)
            UnencryptedByteArray(key.password.value.toByteArray()).let { unencryptedByteArray ->

                return try {
                    kOpenSSL.encrypt(
                        password,
                        encryptionKeyHandler.getHashIterations(key),
                        unencryptedByteArray,
                        dispatchers.default
                    )
                } catch (e: Exception) {
                    key.password.clear()
                    throw AuthenticationException(
                        AuthenticateFlowResponse.Error.FailedToEncryptEncryptionKey
                    )
                } finally {
                    password.clear()
                    unencryptedByteArray.clear()
                }
            }
        }
    }

    private suspend fun encryptTestString(
        key: EncryptionKey,
        kOpenSSL: KOpenSSL
    ): EncryptedString {
        return try {
            kOpenSSL.encrypt(
                key.password,
                encryptionKeyHandler.getHashIterations(key),
                UnencryptedString(Credentials.ENCRYPTION_KEY_TEST_STRING_VALUE),
                dispatchers.default
            )
        } catch (e: Exception) {
            key.password.clear()
            throw AuthenticationException(
                AuthenticateFlowResponse.Error.SetPinFirstTime.FailedToEncryptTestString
            )
        }
    }

    /////////////////
    /// Responses ///
    /////////////////
    private suspend fun processValidPinEntryResponse(
        encryptionKey: EncryptionKey,
        pinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> = flow {
        withContext(dispatchers.default) {
            ArrayList<AuthenticationResponse>(requests.size).let { responses ->
                for (request in requests.sortedBy { it.priority }) {
                    @Exhaustive
                    when (request) {
                        is AuthenticationRequest.LogIn -> {

                            AuthenticationManagerImpl.getEncryptionKey() ?: let {
                                authenticationManagerImpl.setEncryptionKey(encryptionKey)
                            }

                            authenticationManagerImpl.updateAuthenticationState(
                                AuthenticationState.NotRequired, null
                            )
                            responses.add(
                                AuthenticationResponse.Success.Authenticated(request)
                            )
                        }
                        is AuthenticationRequest.ResetPin -> {

                            AuthenticateFlowResponse.ConfirmNewPinEntryToReset
                                .generate(pinEntry, request)
                                ?.let { flowResponseToConfirmNewPinEntryToReset ->
                                    emit(flowResponseToConfirmNewPinEntryToReset)
                                    return@withContext
                                }

                            responses.add(
                                AuthenticationResponse.Success.Authenticated(request)
                            )
                        }
                        is AuthenticationRequest.ConfirmPin -> {
                            responses.add(
                                AuthenticationResponse.Success.Authenticated(request)
                            )
                        }
                        is AuthenticationRequest.GetEncryptionKey -> {

                            AuthenticationManagerImpl.getEncryptionKey() ?: let {
                                authenticationManagerImpl.setEncryptionKey(encryptionKey)
                            }

                            responses.add(
                                AuthenticationResponse.Success.Key(request, encryptionKey)
                            )
                        }
                    }
                }
                emit(AuthenticateFlowResponse.Success.instantiate(responses))
            }
        }
    }
}
