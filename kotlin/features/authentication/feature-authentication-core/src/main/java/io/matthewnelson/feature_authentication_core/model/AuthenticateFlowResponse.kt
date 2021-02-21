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

import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication.coordinator.AuthenticationResponse

sealed class AuthenticateFlowResponse {

    class ConfirmNewPinEntryToReset private constructor(
        private val currentValidPinEntry: PinEntry,
        private val request: AuthenticationRequest.ResetPin
    ): AuthenticateFlowResponse() {

        companion object {
            private val trackerLock = Object()
            private val resetPinCompletionTracker: Array<Int?> by lazy {
                arrayOfNulls(2)
            }

            /**
             * Will issue a request if it has not been fulfilled yet, otherwise returns null.
             * */
            @JvmSynthetic
            internal fun generate(
                validPinEntry: PinEntry,
                request: AuthenticationRequest.ResetPin
            ): ConfirmNewPinEntryToReset? =
                synchronized(trackerLock) {
                    if (resetPinCompletionTracker.contains(request.hashCode())) {
                        null
                    } else {
                        ConfirmNewPinEntryToReset(validPinEntry.clone(), request)
                    }
                }
        }

        /**
         * Call after completion of saving new credentials to inhibit re-issuance when
         * processing request responses.
         * */
        @JvmSynthetic
        internal fun onCompletion() {
            synchronized(trackerLock) {
                for (i in 0 until resetPinCompletionTracker.lastIndex) {
                    resetPinCompletionTracker[i] = resetPinCompletionTracker[i + 1]
                }
                resetPinCompletionTracker[resetPinCompletionTracker.lastIndex] = request.hashCode()
            }
        }

        private var newPinEntry: PinEntry? = null

        @Volatile
        internal var currentValidPinEntryHasBeenCleared: Boolean = false
            private set

        @Synchronized
        fun clearCurrentValidPinEntry() {
            currentValidPinEntryHasBeenCleared = true
            currentValidPinEntry.clearPin()
        }

        @Volatile
        internal var newPinEntryHasBeenCleared: Boolean = false
            private set

        @Synchronized
        fun clearNewPinEntry() {
            newPinEntry?.let { pe ->
                newPinEntryHasBeenCleared = true
                pe.clearPin()
            }
        }

        @JvmSynthetic
        @Synchronized
        internal fun compareConfirmedPinEntry(confirmedPinEntry: PinEntry): Boolean? =
            newPinEntry?.compare(confirmedPinEntry)

        @JvmSynthetic
        @Synchronized
        internal fun getCurrentValidPinEntry(): PinEntry =
            currentValidPinEntry

        @JvmSynthetic
        @Synchronized
        internal fun getNewPinEntry(): PinEntry? =
            newPinEntry

        @Synchronized
        fun setNewPinEntry(newPinEntry: PinEntry?) {
            this.newPinEntry?.clearPin()
            this.newPinEntry = newPinEntry?.clone()
        }
    }

    class ConfirmPinEntryToSetForFirstTime private constructor(
        private val initialPinEntry: PinEntry
    ): AuthenticateFlowResponse() {

        companion object {
            @JvmSynthetic
            internal fun instantiate(initialPinEntry: PinEntry): ConfirmPinEntryToSetForFirstTime =
                ConfirmPinEntryToSetForFirstTime(initialPinEntry)
        }

        @Volatile
        internal var hasBeenCleared: Boolean = false
            private set

        @Synchronized
        fun clearInitialPinEntry() {
            hasBeenCleared = true
            initialPinEntry.clearPin()
        }

        @JvmSynthetic
        @Synchronized
        internal fun compareConfirmedPinEntry(confirmedPinEntry: PinEntry): Boolean =
            initialPinEntry.compare(confirmedPinEntry)

        /**
         * Used within the flow's context under caller's coroutine scope.
         * */
        @JvmSynthetic
        @Synchronized
        internal fun getInitialPinEntry(): PinEntry =
            initialPinEntry
    }

    sealed class Notify: AuthenticateFlowResponse() {
        object DecryptingEncryptionKey: Notify()
        object EncryptingEncryptionKeyWithNewPin: Notify()
        object GeneratingAndEncryptingEncryptionKey: Notify()
    }

    class Success private constructor(
        val value: List<AuthenticationResponse>
    ): AuthenticateFlowResponse() {
        companion object {
            @JvmSynthetic
            internal fun instantiate(responses: List<AuthenticationResponse>): Success =
                Success(responses)
        }
    }

    class WrongPin private constructor(
        val attemptsLeftUntilLockout: Int
    ): AuthenticateFlowResponse() {
        companion object {
            @JvmSynthetic
            internal fun instantiate(attemptsLeftUntilLockout: Int): WrongPin =
                WrongPin(attemptsLeftUntilLockout)
        }
    }

    sealed class Error: AuthenticateFlowResponse() {

        object RequestListEmpty: Error()
        object FailedToEncryptEncryptionKey: Error()
        object FailedToDecryptEncryptionKey: Error()
        class Unclassified(val e: Exception): Error()

        sealed class Authenticate: Error() {
            object InvalidPinEntrySize: Authenticate()
        }

        sealed class ResetPin: Error() {
            object NewPinEntryWasNull: ResetPin()
            object InvalidNewPinEntrySize: ResetPin()
            object InvalidConfirmedPinEntrySize: ResetPin()
            object NewPinDoesNotMatchConfirmedPin: ResetPin()
            object CurrentPinEntryIsNotValid: ResetPin()
            object CredentialsFromPrefsReturnedNull: ResetPin()
            object FailedToStartService: ResetPin()
            object CurrentPinEntryWasCleared: ResetPin()
            object NewPinEntryWasCleared: ResetPin()
        }

        sealed class SetPinFirstTime: Error() {
            object InvalidNewPinEntrySize: SetPinFirstTime()
            object NewPinDoesNotMatchConfirmedPin: SetPinFirstTime()
            object CredentialsFromPrefsReturnedNull: SetPinFirstTime()
            object FailedToStartService: SetPinFirstTime()
            object NewPinEntryWasCleared: SetPinFirstTime()
            object FailedToEncryptTestString: SetPinFirstTime()
        }
    }
}
