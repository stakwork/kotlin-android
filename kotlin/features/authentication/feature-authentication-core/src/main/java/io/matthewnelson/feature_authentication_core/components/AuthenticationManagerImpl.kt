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
package io.matthewnelson.feature_authentication_core.components

import io.matthewnelson.concept_authentication.AuthenticationRequest
import io.matthewnelson.feature_authentication_core.AuthenticationManager
import io.matthewnelson.feature_authentication_core.data.PersistentStorage
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse
import io.matthewnelson.feature_authentication_core.model.PinEntry
import io.matthewnelson.feature_authentication_core.model.PinWriter
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_encryption_key.EncryptionKey
import io.matthewnelson.concept_encryption_key.EncryptionKeyException
import io.matthewnelson.concept_encryption_key.EncryptionKeyHandler
import io.matthewnelson.k_openssl_common.annotations.RawPasswordAccess
import io.matthewnelson.k_openssl_common.clazzes.HashIterations
import kotlinx.coroutines.flow.*

/**
 * Extend this class and implement
 * */
abstract class AuthenticationManagerImpl <T: AuthenticationManagerInitializer>(
    dispatchers: CoroutineDispatchers,
    encryptionKeyHashIterations: HashIterations,
    encryptionKeyHandler: EncryptionKeyHandler,
    persistentStorage: PersistentStorage
): AuthenticationManager() {

    @Suppress("RemoveExplicitTypeArguments")
    private val authenticationProcessor: AuthenticationProcessor<T> by lazy {
        AuthenticationProcessor.instantiate(
            this,
            dispatchers,
            encryptionKeyHashIterations,
            encryptionKeyHandler,
            persistentStorage
        )
    }

    @Volatile
    var isInitialized: Boolean = false
        private set
    private val initializeLock = Object()

    open fun initialize(value: T) {
        synchronized(initializeLock) {
            if (!isInitialized) {
                authenticationProcessor.initializeWrongPinLockout(value)
                isInitialized = true
            }
        }
    }

    @Synchronized
    override fun authenticate(
        pinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> =
        when {
            requests.isEmpty() -> {
                flowOf(AuthenticateFlowResponse.Error.RequestListEmpty)
            }
            pinEntry.getPinWriter().size() < PinWriter.MIN_CHAR_COUNT ||
            pinEntry.getPinWriter().size() > PinWriter.MAX_CHAR_COUNT -> {
                flowOf(AuthenticateFlowResponse.Error.Authenticate.InvalidPinEntrySize)
            }
            else -> {
                authenticationProcessor.authenticate(pinEntry, requests)
            }
        }

    @Synchronized
    override fun resetPin(
        resetPin: AuthenticateFlowResponse.ConfirmNewPinEntryToReset,
        confirmedPinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> =
        when {
            requests.isEmpty() -> {
                flowOf(AuthenticateFlowResponse.Error.RequestListEmpty)
            }
            resetPin.getNewPinEntry() == null -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasNull)
            }
            resetPin.getNewPinEntry()?.getPinWriter()?.size()
                    ?: 0 < PinWriter.MIN_CHAR_COUNT ||
            resetPin.getNewPinEntry()?.getPinWriter()?.size()
                    ?: PinWriter.MAX_CHAR_COUNT + 1 > PinWriter.MAX_CHAR_COUNT -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPin.InvalidNewPinEntrySize)
            }
            confirmedPinEntry.getPinWriter().size() < PinWriter.MIN_CHAR_COUNT ||
            confirmedPinEntry.getPinWriter().size() > PinWriter.MAX_CHAR_COUNT -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPin.InvalidConfirmedPinEntrySize)
            }
            resetPin.currentValidPinEntryHasBeenCleared -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPin.CurrentPinEntryWasCleared)
            }
            resetPin.newPinEntryHasBeenCleared -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasCleared)
            }
            resetPin.compareConfirmedPinEntry(confirmedPinEntry) != true -> {
                flowOf(AuthenticateFlowResponse.Error.ResetPin.NewPinDoesNotMatchConfirmedPin)
            }
            else -> {
                authenticationProcessor.resetPin(resetPin, requests)
            }
        }

    @Synchronized
    override fun setPinFirstTime(
        setPinFirstTime: AuthenticateFlowResponse.ConfirmPinEntryToSetForFirstTime,
        confirmedPinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse> =
        when {
            requests.isEmpty() -> {
                flowOf(AuthenticateFlowResponse.Error.RequestListEmpty)
            }
            confirmedPinEntry.getPinWriter().size() < PinWriter.MIN_CHAR_COUNT ||
            confirmedPinEntry.getPinWriter().size() > PinWriter.MAX_CHAR_COUNT -> {
                flowOf(AuthenticateFlowResponse.Error.SetPinFirstTime.InvalidNewPinEntrySize)
            }
            !setPinFirstTime.compareConfirmedPinEntry(confirmedPinEntry) -> {
                flowOf(AuthenticateFlowResponse.Error.SetPinFirstTime.NewPinDoesNotMatchConfirmedPin)
            }
            setPinFirstTime.hasBeenCleared -> {
                flowOf(AuthenticateFlowResponse.Error.SetPinFirstTime.NewPinEntryWasCleared)
            }
            else -> {
                authenticationProcessor.setPinFirstTime(setPinFirstTime, requests)
            }
        }

    companion object {
        @Volatile
        private var encryptionKey: EncryptionKey? = null
        private val encryptionKeyLock = Object()

        @JvmSynthetic
        internal fun getEncryptionKey(): EncryptionKey? =
            synchronized(encryptionKeyLock) {
                encryptionKey
            }
    }

    @JvmSynthetic
    override fun clearEncryptionKey() {
        setEncryptionKey(null)
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
            Companion.encryptionKey?.password?.clear()
            Companion.encryptionKey = encryptionKey
        }
    }
}
