package io.matthewnelson.concept_authentication

import kotlinx.coroutines.flow.Flow

abstract class BaseAuthenticationCoordinator {

    /**
     * Returns the corresponding response for the provided request, or
     * [AuthenticationResponse.Failure] in some instances.
     *
     * Responses for [AuthenticationRequest.ConfirmPin] submission:
     *   - [AuthenticationResponse.Success.Authenticated]
     *   - [AuthenticationResponse.Failure]
     *
     * Responses for [AuthenticationRequest.GetEncryptionKey] submission:
     *   - [AuthenticationResponse.Success.Key]
     *   - [AuthenticationResponse.Failure]
     *
     * Responses for [AuthenticationRequest.LogIn] submission:
     *   - [AuthenticationResponse.Success.Authenticated]
     *   - [AuthenticationResponse.Failure]
     *
     * Responses for [AuthenticationRequest.ResetPin] submission:
     *   - [AuthenticationResponse.Success.Authenticated]
     *   - [AuthenticationResponse.Failure]
     * */
    abstract suspend fun submitAuthenticationRequest(
        request: AuthenticationRequest
    ): Flow<AuthenticationResponse>
}
