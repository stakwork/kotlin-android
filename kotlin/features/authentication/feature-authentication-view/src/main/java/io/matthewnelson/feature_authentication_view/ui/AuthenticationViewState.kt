package io.matthewnelson.feature_authentication_view.ui

import io.matthewnelson.concept_views.viewstate.ViewState
import io.matthewnelson.feature_authentication_core.model.PinWriter

sealed class AuthenticationViewState: ViewState<AuthenticationViewState>() {
    abstract val pinPadChars: Array<Char>
    abstract val pinLength: Int
    abstract val inputLockState: InputLockState
    val confirmButtonShow: Boolean
        get() = pinLength >= PinWriter.MIN_CHAR_COUNT

    class Idle(
        override val pinLength: Int = 0,

        shufflePin: Boolean = true,

        @Suppress("RemoveExplicitTypeArguments")
        override val pinPadChars: Array<Char> =
            arrayOf<Char>('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
                .let { array ->
                    if (shufflePin) {
                        array.shuffle()
                    }
                    array
                },

        override val inputLockState: InputLockState = InputLockState.Unlocked
    ): AuthenticationViewState()

    class ConfirmPin(
        override val pinLength: Int,
        override val pinPadChars: Array<Char>,
        override val inputLockState: InputLockState
    ): AuthenticationViewState()

    class LogIn(
        override val pinLength: Int,
        override val pinPadChars: Array<Char>,
        override val inputLockState: InputLockState
    ): AuthenticationViewState()

    sealed class ResetPin(
        override val pinLength: Int,
        override val pinPadChars: Array<Char>,
        override val inputLockState: InputLockState
    ): AuthenticationViewState() {

        class Step1(
            pinLength: Int,
            pinPadChars: Array<Char>,
            inputLockState: InputLockState
        ): ResetPin(pinLength, pinPadChars, inputLockState)

        class Step2(
            pinLength: Int,
            pinPadChars: Array<Char>,
            inputLockState: InputLockState
        ): ResetPin(pinLength, pinPadChars, inputLockState)
    }
}