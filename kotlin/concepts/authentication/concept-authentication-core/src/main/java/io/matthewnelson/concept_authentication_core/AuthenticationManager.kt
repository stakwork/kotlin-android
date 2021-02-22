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