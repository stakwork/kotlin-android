package io.matthewnelson.feature_authentication_core.model

internal class AuthenticationException(
    val flowResponseError: AuthenticateFlowResponse.Error
): Exception()
