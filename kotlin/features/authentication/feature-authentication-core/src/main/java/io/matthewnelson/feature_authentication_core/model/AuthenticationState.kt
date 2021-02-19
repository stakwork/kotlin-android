package io.matthewnelson.feature_authentication_core.model

sealed class AuthenticationState {

    object NotRequired: AuthenticationState()

    sealed class Required: AuthenticationState() {
        object InitialLogIn: Required()
        object LoggedOut: Required()
    }
}
