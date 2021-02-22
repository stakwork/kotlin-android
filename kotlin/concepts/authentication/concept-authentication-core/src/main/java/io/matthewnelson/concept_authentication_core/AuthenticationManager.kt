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
package io.matthewnelson.concept_authentication_core

import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication.state.AuthenticationStateManager
import io.matthewnelson.concept_authentication_core.model.ConfirmUserInputToReset
import io.matthewnelson.concept_authentication_core.model.ConfirmUserInputToSetForFirstTime
import io.matthewnelson.concept_authentication_core.model.UserInput
import io.matthewnelson.concept_foreground_state.ForegroundStateManager
import kotlinx.coroutines.flow.Flow

abstract class AuthenticationManager<
        F,
        S: ConfirmUserInputToReset,
        V: ConfirmUserInputToSetForFirstTime,
        >: AuthenticationStateManager,
    ForegroundStateManager // TODO: Move to separate feature
{
    abstract fun getNewUserInput(): UserInput

    abstract fun authenticate(
        userInput: UserInput,
        requests: List<AuthenticationRequest>
    ): Flow<F>

    abstract fun resetPassword(
        resetPasswordResponse: S,
        userInputConfirmation: UserInput,
        requests: List<AuthenticationRequest>
    ): Flow<F>

    abstract fun setPasswordFirstTime(
        setPasswordFirstTimeResponse: V,
        userInputConfirmation: UserInput,
        requests: List<AuthenticationRequest>
    ): Flow<F>
}