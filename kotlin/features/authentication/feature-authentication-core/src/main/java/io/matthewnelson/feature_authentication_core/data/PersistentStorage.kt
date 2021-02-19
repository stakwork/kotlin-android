package io.matthewnelson.feature_authentication_core.data

import io.matthewnelson.feature_authentication_core.model.Credentials
import kotlinx.coroutines.delay

abstract class PersistentStorage {

    protected inner class CredentialString(val value: String)

    protected abstract suspend fun saveCredentialString(credentialString: CredentialString)
    protected abstract suspend fun retrieveCredentialString(): CredentialString?

    @JvmSynthetic
    @Synchronized
    internal suspend fun saveCredentials(credentials: Credentials) {
        saveCredentialString(CredentialString(credentials.toString()))
        delay(25L)
    }

    @JvmSynthetic
    @Synchronized
    internal suspend fun retrieveCredentials(): String? =
        retrieveCredentialString()?.value
}
