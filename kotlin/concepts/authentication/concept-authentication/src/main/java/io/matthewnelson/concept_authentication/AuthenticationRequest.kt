package io.matthewnelson.concept_authentication

sealed class AuthenticationRequest {

    abstract val priority: Int

    class ConfirmPin: AuthenticationRequest() {
        override val priority: Int = 3
    }

    class GetEncryptionKey: AuthenticationRequest() {
        override val priority: Int = 4
    }

    class LogIn: AuthenticationRequest() {
        override val priority: Int = 1
    }

    class ResetPin: AuthenticationRequest() {
        override val priority: Int = 2
    }
}
