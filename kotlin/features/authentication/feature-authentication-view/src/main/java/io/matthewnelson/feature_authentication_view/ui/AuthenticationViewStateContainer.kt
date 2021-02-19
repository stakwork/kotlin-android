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

import io.matthewnelson.concept_views.viewstate.ViewStateContainer
import io.matthewnelson.concept_views.viewstate.value

class AuthenticationViewStateContainer(
    shufflePin: Boolean
): ViewStateContainer<AuthenticationViewState>(
    AuthenticationViewState.Idle(shufflePin = shufflePin)
) {
    override fun updateViewState(viewState: AuthenticationViewState) {
        throw IllegalAccessException("Updating the AuthenticationViewState is not supported")
    }

    private val pinPadChars: Array<Char> = viewStateFlow.value.pinPadChars.copyOf()

    @JvmSynthetic
    internal fun getPinPadChars(): Array<Char> =
        pinPadChars

    @JvmSynthetic
    internal fun internalUpdateViewState(viewState: AuthenticationViewState) {
        super.updateViewState(viewState)
    }

    @JvmSynthetic
    internal fun updateCurrentViewState(
        pinLength: Int = this.value.pinLength,
        pinPadChars: Array<Char> = this.pinPadChars,
        inputLockState: InputLockState = viewStateFlow.value.inputLockState
    ) {
        when (viewStateFlow.value) {
            is AuthenticationViewState.ConfirmPin -> {
                AuthenticationViewState.ConfirmPin(pinLength, pinPadChars, inputLockState)
            }
            is AuthenticationViewState.Idle -> {
                AuthenticationViewState.Idle(
                    pinLength = pinLength,
                    pinPadChars = pinPadChars,
                    inputLockState = inputLockState
                )
            }
            is AuthenticationViewState.LogIn -> {
                AuthenticationViewState.LogIn(pinLength, pinPadChars, inputLockState)
            }
            is AuthenticationViewState.ResetPin.Step1 -> {
                AuthenticationViewState.ResetPin.Step1(pinLength, pinPadChars, inputLockState)
            }
            is AuthenticationViewState.ResetPin.Step2 -> {
                AuthenticationViewState.ResetPin.Step2(pinLength, pinPadChars, inputLockState)
            }
        }.let { viewState ->
            super.updateViewState(viewState)
        }
    }

}