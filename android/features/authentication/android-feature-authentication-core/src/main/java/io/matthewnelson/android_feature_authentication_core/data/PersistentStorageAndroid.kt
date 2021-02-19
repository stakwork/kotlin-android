package io.matthewnelson.android_feature_authentication_core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.matthewnelson.feature_authentication_core.data.PersistentStorage
import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PersistentStorageAndroid(
    context: Context,
    masterKeyAlias: MasterKeyAlias,
    authenticationSharedPrefsName: AuthenticationSharedPrefsName,
    private val dispatchers: CoroutineDispatchers
): PersistentStorage() {

    private companion object {
        private const val CREDENTIALS = "CREDENTIALS"
    }

    private val authenticationPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context.applicationContext,
            authenticationSharedPrefsName.value,
            MasterKey.Builder(context, masterKeyAlias.value)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(false)
                .setRequestStrongBoxBacked(false)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveCredentialString(credentialString: CredentialString) {
        withContext(dispatchers.io) {
            authenticationPrefs.edit().putString(CREDENTIALS, credentialString.value)
                .let { editor ->
                    if (!editor.commit()) {
                        editor.apply()
                        delay(250L)
                    }
                }
        }
    }

    override suspend fun retrieveCredentialString(): CredentialString? {
        return withContext(dispatchers.io) {
            authenticationPrefs.getString(CREDENTIALS, null)
        }?.let { string ->
            CredentialString(string)
        }
    }
}
