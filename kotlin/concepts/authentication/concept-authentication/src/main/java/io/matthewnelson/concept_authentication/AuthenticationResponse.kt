package io.matthewnelson.concept_authentication

import io.matthewnelson.concept_encryption_key.EncryptionKey

sealed class AuthenticationResponse(val authenticationRequest: AuthenticationRequest) {

    sealed class Success(request: AuthenticationRequest): AuthenticationResponse(request) {

        class Authenticated(
            request: AuthenticationRequest
        ): Success(request)

        // TODO: Make an encryption Key generator interface to implement
        //  so lib users can generate whatever key they like.
        class Key(
            request: AuthenticationRequest,
            val encryptionKey: EncryptionKey
        ): Success(request)
    }

    class Failure(
        request: AuthenticationRequest
    ): AuthenticationResponse(request)
}
