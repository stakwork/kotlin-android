package io.matthewnelson.feature_authentication_view.navigation

import io.matthewnelson.concept_authentication.AuthenticationRequest
import io.matthewnelson.concept_authentication.AuthenticationResponse
import io.matthewnelson.feature_authentication_core.AuthenticationCoordinator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class AuthenticationViewCoordinator<T>(
    private val authenticationNavigator: AuthenticationNavigator<T>
): AuthenticationCoordinator() {

    @JvmSynthetic
    internal fun getAuthenticationRequestSharedFlow(): SharedFlow<AuthenticationRequest> =
        _authenticationRequestSharedFlow.asSharedFlow()

    override suspend fun navigateToAuthenticationView() {
        authenticationNavigator.toAuthenticationView()
    }

    @JvmSynthetic
    internal suspend fun completeAuthentication(responses: List<AuthenticationResponse>) {
        for (response in responses) {
            _authenticationResponseSharedFlow.emit(response)
        }
        authenticationNavigator.popBackStack()
        delay(300L)
    }
}