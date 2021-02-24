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
package io.matthewnelson.concept_authentication.coordinator

sealed class AuthenticationRequest {

    abstract val priority: Int

    class ConfirmPin: AuthenticationRequest() {
        override val priority: Int = 3
    }

    /**
     * If the user is logged in (The [io.matthewnelson.concept_encryption_key.EncryptionKey]
     * hasn't been cleared and state is
     * [io.matthewnelson.concept_authentication.state.AuthenticationState.NotRequired]), this
     * request will automatically return the key.
     *
     * Setting the [navigateToAuthenticationViewOnFailure] to `true` will navigate
     * to the View for the user to authenticate in the event they are not logged in. The
     * Coordinator will suspend until the request is authenticated, and return the key.
     *
     * Alternatively, setting [navigateToAuthenticationViewOnFailure] to `false` will return
     * [io.matthewnelson.concept_authentication.coordinator.AuthenticationResponse.Failure]
     * if state is requiring authentication, and then if the
     * [io.matthewnelson.concept_encryption_key.EncryptionKey] is unavailable.
     * */
    class GetEncryptionKey(
        val navigateToAuthenticationViewOnFailure: Boolean = true
    ): AuthenticationRequest() {
        override val priority: Int = 4
    }

    class LogIn: AuthenticationRequest() {
        override val priority: Int = 1
    }

    class ResetPin: AuthenticationRequest() {
        override val priority: Int = 2
    }
}
