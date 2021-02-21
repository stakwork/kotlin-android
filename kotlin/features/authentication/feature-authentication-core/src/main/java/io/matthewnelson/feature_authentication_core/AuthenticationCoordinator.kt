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
import io.matthewnelson.concept_authentication.coordinator.BaseAuthenticationCoordinator
import io.matthewnelson.feature_authentication_core.components.AuthenticationManagerImpl
import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication.coordinator.AuthenticationResponse
import io.matthewnelson.feature_authentication_core.model.AuthenticationState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive

abstract class AuthenticationCoordinator: BaseAuthenticationCoordinator() {

    @Suppress("RemoveExplicitTypeArguments", "PropertyName")
    protected val _authenticationResponseSharedFlow: MutableSharedFlow<AuthenticationResponse> by lazy {
        MutableSharedFlow<AuthenticationResponse>(0, 1)
    }

    @Suppress("RemoveExplicitTypeArguments", "PropertyName")
    protected val _authenticationRequestSharedFlow: MutableSharedFlow<AuthenticationRequest> by lazy {
        MutableSharedFlow<AuthenticationRequest>(0, 1)
    }

    protected abstract suspend fun navigateToAuthenticationView()

    override suspend fun submitAuthenticationRequest(
        request: AuthenticationRequest
    ): Flow<AuthenticationResponse> {
        @Exhaustive
        when (request) {
            is AuthenticationRequest.GetEncryptionKey -> {
                when (AuthenticationManager.authenticationStateFlow.value) {
                    is AuthenticationState.NotRequired -> {
                        AuthenticationManagerImpl.getEncryptionKey()?.let { key ->
                            return flowOf(
                                AuthenticationResponse.Success.Key(request, key)
                            )
                        }
                    }
                    else -> {}
                }
            }
            is AuthenticationRequest.LogIn -> {
                when (AuthenticationManager.authenticationStateFlow.value) {
                    is AuthenticationState.NotRequired -> {
                        AuthenticationManagerImpl.getEncryptionKey()?.let {
                            return flowOf(
                                AuthenticationResponse.Success.Authenticated(request)
                            )
                        }
                    }
                    else -> {}
                }
            }
            is AuthenticationRequest.ConfirmPin,
            is AuthenticationRequest.ResetPin -> {}
        }

        navigateToAuthenticationView()

        while (
            currentCoroutineContext().isActive &&
            _authenticationRequestSharedFlow.subscriptionCount.value == 0
        ) {
            delay(50L)
        }

        _authenticationRequestSharedFlow.emit(request)

        return flow {
            _authenticationResponseSharedFlow.asSharedFlow().collect { response ->
                if (response.authenticationRequest == request) {
                    emit(response)
                }
            }
        }
    }
}
