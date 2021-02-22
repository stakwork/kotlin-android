package io.matthewnelson.concept_authentication_core

import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication_core.model.ConfirmUserInputToReset
import io.matthewnelson.concept_authentication_core.model.ConfirmUserInputToSetForFirstTime
import io.matthewnelson.concept_authentication_core.model.UserInput
import kotlinx.coroutines.flow.Flow

abstract class AuthenticationManager<
        F,
        U: UserInput<U>,
        S: ConfirmUserInputToReset<U>,
        V: ConfirmUserInputToSetForFirstTime<U>,
        >
{

    abstract fun authenticate(
        userInput: U,
        requests: List<AuthenticationRequest>
    ): Flow<F>

    abstract fun resetPin(
        resetPinResponse: S,
        userInputConfirmation: U,
        requests: List<AuthenticationRequest>
    ): Flow<F>

    abstract fun setPinFirstTime(
        setPinFirstTimeResponse: V,
        userInputConfirmation: U,
        requests: List<AuthenticationRequest>
    ): Flow<F>
}