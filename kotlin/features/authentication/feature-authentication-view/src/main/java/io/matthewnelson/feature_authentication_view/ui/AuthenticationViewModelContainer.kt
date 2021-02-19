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
package io.matthewnelson.feature_authentication_view.ui

import app.cash.exhaustive.Exhaustive
import io.matthewnelson.concept_authentication.AuthenticationRequest
import io.matthewnelson.concept_authentication.AuthenticationResponse
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import io.matthewnelson.concept_views.viewstate.value
import io.matthewnelson.feature_authentication_core.AuthenticationManager
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse
import io.matthewnelson.feature_authentication_core.model.AuthenticationState
import io.matthewnelson.feature_authentication_core.model.ForegroundState
import io.matthewnelson.feature_authentication_core.model.PinEntry
import io.matthewnelson.feature_authentication_view.components.AuthenticationRequestTracker
import io.matthewnelson.feature_authentication_view.components.ConfirmPressAction
import io.matthewnelson.feature_authentication_view.navigation.AuthenticationViewCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthenticationViewModelContainer<T>(
    val authenticationManager: AuthenticationManager,
    val dispatchers: CoroutineDispatchers,
    val eventHandler: AuthenticationEventHandler,
    val coordinator: AuthenticationViewCoordinator<T>,
    shufflePinNumbers: Boolean,
    val viewModelScope: CoroutineScope
) {
    private val pinEntry: PinEntry = PinEntry()
    private val authenticationRequestTracker = AuthenticationRequestTracker()
    private val confirmPressAction = ConfirmPressAction()
    private val viewStateUpdateLock = Object()
    private var confirmPressJob: Job? = null

    @Suppress("RemoveExplicitTypeArguments")
    val viewStateContainer: AuthenticationViewStateContainer by lazy {
        AuthenticationViewStateContainer(shufflePinNumbers)
    }

    init {
        viewModelScope.launch(dispatchers.default) {

            // When the view model gets cancelled, so will the supervisor scope.
            // Clean up.
            launch {
                getAuthenticationFinishedStateFlow().collect {}
            }.invokeOnCompletion {
                pinEntry.clearPin()
                confirmPressAction.updateAction(ConfirmPressAction.Action.Authenticate)
            }

            launch {
                coordinator.getAuthenticationRequestSharedFlow().collect { request ->
                    if (!authenticationRequestTracker.addRequest(request)) {
                        return@collect
                    }

                    confirmPressJob?.join()

                    synchronized(viewStateUpdateLock) {
                        pinEntry.clearPin()
                        confirmPressAction.updateAction(ConfirmPressAction.Action.Authenticate)

                        val viewState = when (request) {
                            is AuthenticationRequest.ResetPin,
                            is AuthenticationRequest.ConfirmPin -> {
                                AuthenticationViewState.ConfirmPin(
                                    pinEntry.pinLengthStateFlow.value,
                                    viewStateContainer.getPinPadChars(),
                                    InputLockState.Unlocked
                                )
                            }
                            is AuthenticationRequest.LogIn,
                            is AuthenticationRequest.GetEncryptionKey -> {
                                AuthenticationViewState.LogIn(
                                    pinEntry.pinLengthStateFlow.value,
                                    viewStateContainer.getPinPadChars(),
                                    InputLockState.Unlocked
                                )
                            }
                        }

                        viewStateContainer.internalUpdateViewState(viewState)
                    }
                }
            }

            launch {
                pinEntry.pinLengthStateFlow.collect { pinLength ->
                    synchronized(viewStateUpdateLock) {
                        if (pinLength == viewStateContainer.value.pinLength) {
                            return@collect
                        }

                        viewStateContainer.updateCurrentViewState(pinLength)
                    }
                }
            }

            // Clear pin entry if moved to background
            launch {
                AuthenticationManager.foregroundStateFlow.collect { state ->
                    if (state is ForegroundState.Background && confirmPressJob?.isActive != true) {
                        pinEntry.clearPin()
                    }
                }
            }
        }
    }

    ////////////////////////////////
    /// Authentication Responses ///
    ////////////////////////////////
    @Suppress("RemoveExplicitTypeArguments", "PrivatePropertyName")
    private val _authenticationFinishedStateFlow: MutableStateFlow<List<AuthenticationResponse>?> by lazy {
        MutableStateFlow<List<AuthenticationResponse>?>(null)
    }

    /*
     * Enables tying the execution of returning responses to callers into the UI such that
     * completeAuthentication is only called upon when in foreground. Needed b/c if user
     * is in the middle of authenticating and sends app to background, then returns to foreground
     * they can be logged out by the automatic background logout feature. The new login request
     * will need to be processed.
     * */
    fun getAuthenticationFinishedStateFlow(): StateFlow<List<AuthenticationResponse>?> =
        _authenticationFinishedStateFlow.asStateFlow()

    fun completeAuthentication(responses: List<AuthenticationResponse>) {
        if (
            AuthenticationManager.authenticationStateFlow.value !is AuthenticationState.NotRequired ||
            authenticationRequestTracker.getRequestListSize() != responses.size
        ) {
            _authenticationFinishedStateFlow.value = null
            pinEntry.clearPin()
            synchronized(viewStateUpdateLock) {
                if (viewStateContainer.value.inputLockState !is InputLockState.Unlocked) {
                    viewStateContainer.updateCurrentViewState(
                        pinLength = pinEntry.pinLengthStateFlow.value,
                        inputLockState = InputLockState.Unlocked
                    )
                }
            }
        } else {
            submitAuthenticationResponses(responses)
        }
    }

    private var completeAuthenticationJob: Job? = null
    private val submitAuthenticationResponseLock = Object()
    private fun submitAuthenticationResponses(responses: List<AuthenticationResponse>) {
        synchronized(submitAuthenticationResponseLock) {
            if (completeAuthenticationJob?.isActive == true) {
                return
            }

            completeAuthenticationJob = viewModelScope.launch(dispatchers.default) {
                coordinator.completeAuthentication(responses)
            }
        }
    }

    /////////////////////////
    /// Device Back Press ///
    /////////////////////////
    sealed class HandleBackPressResponse {
        object Minimize: HandleBackPressResponse()
        object DoNothing: HandleBackPressResponse()
    }

    fun handleDeviceBackPress(): HandleBackPressResponse =
        when {
            AuthenticationManager.authenticationStateFlow.value is AuthenticationState.Required -> {
                HandleBackPressResponse.Minimize
            }
            viewStateContainer.value is AuthenticationViewState.ResetPin.Step2 &&
                    confirmPressJob?.isActive == true -> {
                HandleBackPressResponse.Minimize
            }
            else -> {
                viewModelScope.launch(dispatchers.default) {
                    // Need to delay a tad longer than the view state execution delay
                    // from fragment to ensure that it goes off first when view
                    // comes back into focus, as that is automated by response from
                    // authentication manager and this is processing user input.
                    delay(125L)

                    authenticationRequestTracker.getRequestsList().let { requests ->
                        ArrayList<AuthenticationResponse>(requests.size).let { responses ->
                            for (request in requests) {
                                responses.add(AuthenticationResponse.Failure(request))
                            }
                            submitAuthenticationResponses(responses)
                        }
                    }
                }
                HandleBackPressResponse.DoNothing
            }
        }

    //////////////////
    /// User Input ///
    //////////////////
    fun backSpacePress() {
        viewModelScope.launch(dispatchers.mainImmediate) {
            eventHandler.produceHapticFeedback()
        }

        try {
            pinEntry.dropLastCharacter()
        } catch (e: IllegalArgumentException) {
            // TODO: shake animation the pin hint container
        }
    }

    /**
     * Returns true if the character was added, false if it was not (max length was hit)
     * */
    fun numPadPress(c: Char): Boolean {
        viewModelScope.launch(dispatchers.mainImmediate) {
            eventHandler.produceHapticFeedback()
        }

        return try {
            pinEntry.addCharacter(c)
            true
        } catch (e: IllegalArgumentException) {
            // TODO: shake animation the pin hint container
            false
        }
    }

    fun confirmPress() {
        viewModelScope.launch(dispatchers.mainImmediate) {
            eventHandler.produceHapticFeedback()
        }

        if (confirmPressJob?.isActive == true) {
            return
        }

        confirmPressJob = viewModelScope.launch(dispatchers.default) {
            confirmPressAction.getAction().let { action ->
                @Exhaustive
                when (action) {
                    is ConfirmPressAction.Action.Authenticate -> {
                        processResponseFlow(
                            authenticationManager.authenticate(
                                pinEntry,
                                authenticationRequestTracker.getRequestsList()
                            )
                        )
                    }
                    is ConfirmPressAction.Action.ResetPin -> {
                        when (viewStateContainer.value) {
                            is AuthenticationViewState.ResetPin.Step1 -> {
                                action.flowResponseResetPin.setNewPinEntry(pinEntry)
                                synchronized(viewStateUpdateLock) {
                                    pinEntry.clearPin()
                                    viewStateContainer.internalUpdateViewState(
                                        AuthenticationViewState.ResetPin.Step2(
                                            0,
                                            viewStateContainer.getPinPadChars(),
                                            InputLockState.Unlocked
                                        )
                                    )
                                }
                            }
                            is AuthenticationViewState.ResetPin.Step2 -> {
                                processResponseFlow(
                                    authenticationManager.resetPin(
                                        action.flowResponseResetPin,
                                        pinEntry,
                                        authenticationRequestTracker.getRequestsList()
                                    )
                                )
                            }
                            else -> {
                                // TODO: Something's amiss. figure out.
                            }
                        }
                    }
                    is ConfirmPressAction.Action.SetPinFirstTime -> {
                        processResponseFlow(
                            authenticationManager.setPinFirstTime(
                                action.flowResponseSetPinFirstTime,
                                pinEntry,
                                authenticationRequestTracker.getRequestsList()
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun processResponseFlow(responseFlow: Flow<AuthenticateFlowResponse>) {
        synchronized(viewStateUpdateLock) {
            viewStateContainer.updateCurrentViewState(inputLockState = InputLockState.Locked.Idle)
        }

        responseFlow.collect { response ->
            @Exhaustive
            when (response) {
                is AuthenticateFlowResponse.Success -> {
                    _authenticationFinishedStateFlow.value = response.value
                }
                is AuthenticateFlowResponse.ConfirmNewPinEntryToReset -> {
                    synchronized(viewStateUpdateLock) {
                        viewStateContainer.internalUpdateViewState(
                            AuthenticationViewState.ResetPin.Step1(
                                pinEntry.pinLengthStateFlow.value,
                                viewStateContainer.getPinPadChars(),
                                viewStateContainer.value.inputLockState
                            )
                        )
                        confirmPressAction.updateAction(
                            ConfirmPressAction.Action.ResetPin.instantiate(response)
                        )
                    }
                }
                is AuthenticateFlowResponse.ConfirmPinEntryToSetForFirstTime -> {
                    synchronized(viewStateUpdateLock) {
                        viewStateContainer.internalUpdateViewState(
                            AuthenticationViewState.ConfirmPin(
                                pinEntry.pinLengthStateFlow.value,
                                viewStateContainer.getPinPadChars(),
                                viewStateContainer.value.inputLockState
                            )
                        )
                        confirmPressAction.updateAction(
                            ConfirmPressAction.Action.SetPinFirstTime.instantiate(response)
                        )
                    }
                }
                is AuthenticateFlowResponse.Notify -> {
                    synchronized(viewStateUpdateLock) {
                        if (viewStateContainer.value.inputLockState is InputLockState.Locked) {
                            viewStateContainer.updateCurrentViewState(
                                inputLockState = InputLockState.Locked.Notify(response)
                            )
                        } else {
                            return@collect
                        }
                    }
                }
                is AuthenticateFlowResponse.WrongPin -> {
                    viewModelScope.launch(dispatchers.mainImmediate) {
                        if (response.attemptsLeftUntilLockout == 1) {
                            eventHandler.onOneMoreAttemptUntilLockout()
                        } else {
                            eventHandler.onWrongPin()
                        }
                    }
                }
                is AuthenticateFlowResponse.Error.Authenticate -> {
                    processAuthenticateError(response)
                }
                is AuthenticateFlowResponse.Error.ResetPin -> {
                    processResetPinError(response)
                }
                is AuthenticateFlowResponse.Error.SetPinFirstTime -> {
                    processSetPinFirstTimeError(response)
                }
                is AuthenticateFlowResponse.Error.FailedToDecryptEncryptionKey -> {
                    // TODO: Implement
                }
                is AuthenticateFlowResponse.Error.FailedToEncryptEncryptionKey -> {
                    // TODO: Implement
                }
                is AuthenticateFlowResponse.Error.RequestListEmpty -> {
                    // TODO: Implement
                }
                is AuthenticateFlowResponse.Error.Unclassified -> {
                    response.e.printStackTrace()
                }
            }
        }

        if (getAuthenticationFinishedStateFlow().value == null) {
            synchronized(viewStateUpdateLock) {
                pinEntry.clearPin()
                viewStateContainer.updateCurrentViewState(inputLockState = InputLockState.Unlocked)
            }
        }
    }

    private suspend fun processAuthenticateError(
        error: AuthenticateFlowResponse.Error.Authenticate
    ) {
        @Exhaustive
        when (error) {
            AuthenticateFlowResponse.Error.Authenticate.InvalidPinEntrySize -> {
                // Will never ever happen from here b/c confirm button does not
                // show until min characters are met.
                // TODO: Implement
            }
        }
    }

    private suspend fun processResetPinError(
        error: AuthenticateFlowResponse.Error.ResetPin
    ) {
        @Exhaustive
        when (error) {
            AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasNull -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.InvalidNewPinEntrySize -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.InvalidConfirmedPinEntrySize -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.NewPinDoesNotMatchConfirmedPin -> {
                eventHandler.onNewPinDoesNotMatchConfirmedPin()
            }
            AuthenticateFlowResponse.Error.ResetPin.CurrentPinEntryIsNotValid -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.CredentialsFromPrefsReturnedNull -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.FailedToStartService -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.CurrentPinEntryWasCleared -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.ResetPin.NewPinEntryWasCleared -> {
                // TODO: Implement
            }
        }
    }

    private suspend fun processSetPinFirstTimeError(
        error: AuthenticateFlowResponse.Error.SetPinFirstTime
    ) {
        @Exhaustive
        when (error) {
            AuthenticateFlowResponse.Error.SetPinFirstTime.InvalidNewPinEntrySize -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.SetPinFirstTime.NewPinDoesNotMatchConfirmedPin -> {
                eventHandler.onNewPinDoesNotMatchConfirmedPin()
            }
            AuthenticateFlowResponse.Error.SetPinFirstTime.CredentialsFromPrefsReturnedNull -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.SetPinFirstTime.FailedToStartService -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.SetPinFirstTime.NewPinEntryWasCleared -> {
                // TODO: Implement
            }
            AuthenticateFlowResponse.Error.SetPinFirstTime.FailedToEncryptTestString -> {
                // TODO: Implement
            }
        }
    }
}