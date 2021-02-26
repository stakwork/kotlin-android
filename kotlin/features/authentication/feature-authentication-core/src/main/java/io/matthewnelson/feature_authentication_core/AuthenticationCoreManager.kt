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
package io.matthewnelson.feature_authentication_core

import app.cash.exhaustive.Exhaustive
import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication.coordinator.AuthenticationResponse
import io.matthewnelson.concept_authentication.state.AuthenticationState
import io.matthewnelson.concept_authentication_core.AuthenticationManager
import io.matthewnelson.concept_authentication_core.model.UserInput
import io.matthewnelson.feature_authentication_core.data.AuthenticationCoreStorage
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.concept_encryption_key.EncryptionKeyException
import io.matthewnelson.concept_encryption_key.EncryptionKeyHandler
import io.matthewnelson.feature_authentication_core.components.AuthenticationManagerInitializer
import io.matthewnelson.feature_authentication_core.components.AuthenticationProcessor
import io.matthewnelson.feature_authentication_core.model.UserInputWriter
import io.matthewnelson.k_openssl_common.annotations.RawPasswordAccess
import io.matthewnelson.k_openssl_common.clazzes.HashIterations
import io.matthewnelson.k_openssl_common.clazzes.clear
import kotlinx.coroutines.flow.*

/**
 * Extend this class and implement
 * */
abstract class AuthenticationCoreManager <T: AuthenticationManagerInitializer>(
    dispatchers: CoroutineDispatchers,
    encryptionKeyHashIterations: HashIterations,
    encryptionKeyHandler: EncryptionKeyHandler,
    authenticationCoreStorage: AuthenticationCoreStorage
): AuthenticationManager<
        AuthenticateFlowResponse,
        AuthenticateFlowResponse.PasswordConfirmedForReset,
        AuthenticateFlowResponse.ConfirmInputToSetForFirstTime
        >()
{

    ///////////////////////////
    /// AuthenticationState ///
    ///////////////////////////
    private val _authenticationStateFlow: MutableStateFlow<AuthenticationState> by lazy {
        MutableStateFlow(AuthenticationState.Required.InitialLogIn)
    }

    override val authenticationStateFlow: StateFlow<AuthenticationState>
        get() = _authenticationStateFlow.asStateFlow()

    @JvmSynthetic
    @Suppress("UNUSED_PARAMETER")
    internal fun updateAuthenticationState(state: AuthenticationState, any: Any?) {
        @Exhaustive
        when (state) {
            is AuthenticationState.NotRequired -> {}
            is AuthenticationState.Required -> {
                setEncryptionKey(null)
            }
        }
        _authenticationStateFlow.value = state
    }

    protected fun updateAuthenticationState(state: AuthenticationState) {
        updateAuthenticationState(state, null)
    }

    //////////////////////
    /// Initialization ///
    //////////////////////
    @Volatile
    var isInitialized: Boolean = false
        private set
    private val initializeLock = Object()

    open fun initialize(value: T) {
        synchronized(initializeLock) {
            if (!isInitialized) {
                minUserInputLength = value.minimumUserInputLength
                maxUserInputLength = value.maximumUserInputLength
                authenticationProcessor.initializeWrongPinLockout(value)
                isInitialized = true
            }
        }
    }

    companion object {
        var minUserInputLength: Int = 8
            private set
        var maxUserInputLength: Int = 42
            private set
    }

    //////////////////////
    /// Authentication ///
    //////////////////////
    @Suppress("RemoveExplicitTypeArguments")
    private val authenticationProcessor: AuthenticationProcessor<T> by lazy {
        AuthenticationProcessor.instantiate(
            this,
            dispatchers,
            encryptionKeyHashIterations,
            encryptionKeyHandler,
            authenticationCoreStorage
        )
    }

    override suspend fun isAnEncryptionKeySet(): Boolean {
        return authenticationProcessor.isAnEncryptionKeySet()
    }

    override fun getNewUserInput(): UserInput {
        return UserInputWriter.instantiate()
    }

    @Synchronized
    @OptIn(RawPasswordAccess::class)
    override fun authenticate(
        request: AuthenticationRequest.LogIn
    ): Flow<AuthenticationResponse> =
        request.encryptionKey?.let { key ->
            if (key.value.isEmpty()) {
                flowOf(
                    AuthenticationResponse.Failure(request)
                )
            }
            authenticationProcessor.authenticate(key, request)
        } ?: flowOf(
            AuthenticationResponse.Failure(request)
        )

    @Synchronized
    override fun authenticate(
        userInput: UserInput,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> =
        when {
            requests.isEmpty() -> {
                flowOf(AuthenticateFlowResponse.Error.RequestListEmpty)
            }
            try {
                (userInput as UserInputWriter).size() < minUserInputLength ||
                userInput.size() > maxUserInputLength
            } catch(e: ClassCastException) {
                // TODO: create new error to submit back instead of InvalidPasswordEntrySize
                true
            } -> {
                flowOf(AuthenticateFlowResponse.Error.Authenticate.InvalidPasswordEntrySize)
            }
            else -> {
                authenticationProcessor.authenticate(userInput as UserInputWriter, requests)
            }
        }

    @Synchronized
    override fun resetPassword(
        resetPasswordResponse: AuthenticateFlowResponse.PasswordConfirmedForReset,
        userInputConfirmation: UserInput,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> =
        when {
            requests.isEmpty() -> {
                flowOf(AuthenticateFlowResponse.Error.RequestListEmpty)
            }
            resetPasswordResponse.getNewPasswordToBeSet() == null -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPassword.NewPasswordEntryWasNull)
            }

            resetPasswordResponse.getNewPasswordToBeSet()?.size() ?: 0
                    < minUserInputLength ||
            resetPasswordResponse.getNewPasswordToBeSet()?.size() ?: maxUserInputLength + 1
                    > maxUserInputLength -> {
                        flowOf(AuthenticateFlowResponse.Error.ResetPassword.InvalidNewPasswordEntrySize)
                    }

            try {
                (userInputConfirmation as UserInputWriter).size() < minUserInputLength ||
                        userInputConfirmation.size() > maxUserInputLength
            } catch (e: ClassCastException) {
                // TODO: create new error to submit back instead of InvalidPasswordEntrySize
                true
            } -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPassword.InvalidConfirmedPasswordEntrySize)
            }
            resetPasswordResponse.originalValidatedUserInputHasBeenCleared -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPassword.CurrentPasswordEntryWasCleared)
            }
            resetPasswordResponse.newPasswordHasBeenCleared -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPassword.NewPasswordEntryWasCleared)
            }
            resetPasswordResponse.compareNewPasswordWithConfirmationInput(userInputConfirmation) != true -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPassword.NewPinDoesNotMatchConfirmedPassword)
            }
            else -> {
                authenticationProcessor.resetPassword(resetPasswordResponse, requests)
            }
        }

    @Synchronized
    override fun setPasswordFirstTime(
        setPasswordFirstTimeResponse: AuthenticateFlowResponse.ConfirmInputToSetForFirstTime,
        userInputConfirmation: UserInput,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> =
        when {
            requests.isEmpty() -> {
                flowOf(AuthenticateFlowResponse.Error.RequestListEmpty)
            }
            try {
                (userInputConfirmation as UserInputWriter).size() < minUserInputLength ||
                        userInputConfirmation.size() > maxUserInputLength
            } catch (e: ClassCastException) {
                // TODO: create new error to submit back instead of InvalidPasswordEntrySize
                true
            } -> {
                flowOf(AuthenticateFlowResponse.Error.SetPasswordFirstTime.InvalidNewPasswordEntrySize)
            }
            !setPasswordFirstTimeResponse.compareInitialInputWithConfirmedInput(userInputConfirmation) -> {
                flowOf(AuthenticateFlowResponse.Error.SetPasswordFirstTime.NewPasswordDoesNotMatchConfirmedPassword)
            }
            setPasswordFirstTimeResponse.initialUserInputHasBeenCleared -> {
                flowOf(AuthenticateFlowResponse.Error.SetPasswordFirstTime.NewPasswordEntryWasCleared)
            }
            else -> {
                authenticationProcessor.setPasswordFirstTime(setPasswordFirstTimeResponse, requests)
            }
        }

    //////////////////////
    /// Encryption Key ///
    //////////////////////

    @Volatile
    private var encryptionKey: EncryptionKey? = null
    private val encryptionKeyLock = Object()

    @JvmSynthetic
    internal fun getEncryptionKey(): EncryptionKey? =
        synchronized(encryptionKeyLock) {
            encryptionKey
        }

    @JvmSynthetic
    internal fun getEncryptionKeyCopy(): EncryptionKey? =
        synchronized(encryptionKeyLock) {
            encryptionKey?.let { key ->
                try {
                    @OptIn(RawPasswordAccess::class)
                    authenticationProcessor.encryptionKeyHandler
                        .storeCopyOfEncryptionKey(key.password.value)
                } catch (e: EncryptionKeyException) {
                    null
                }
            }
        }

    @JvmSynthetic
    internal fun setEncryptionKey(encryptionKey: EncryptionKey?) {
        synchronized(encryptionKeyLock) {
            this.encryptionKey?.password?.clear()
            this.encryptionKey = encryptionKey
        }
    }
}
