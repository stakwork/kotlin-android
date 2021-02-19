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