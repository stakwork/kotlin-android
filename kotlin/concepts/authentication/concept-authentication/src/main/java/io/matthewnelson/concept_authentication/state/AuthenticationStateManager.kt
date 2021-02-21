package io.matthewnelson.concept_authentication.state

import kotlinx.coroutines.flow.StateFlow

abstract class AuthenticationStateManager {
    abstract val authenticationStateFlow: StateFlow<AuthenticationState>
}