package io.matthewnelson.feature_authentication_core

import app.cash.exhaustive.Exhaustive
import io.matthewnelson.concept_authentication.BaseAuthenticationCoordinator
import io.matthewnelson.feature_authentication_core.components.AuthenticationManagerImpl
import io.matthewnelson.concept_authentication.AuthenticationRequest
import io.matthewnelson.concept_authentication.AuthenticationResponse
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
