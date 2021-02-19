package io.matthewnelson.android_feature_authentication_core.components

import android.app.Application
import io.matthewnelson.feature_authentication_core.components.AuthenticationManagerInitializer

class AuthenticationManagerInitializerAndroid(
    val application: Application,
    val backgroundLogOutTime: Long = 0L,
    override val wrongPinAttemptsUntilLockedOut: Int = 0,
    override val wrongPinLockoutDuration: Long = 0L
): AuthenticationManagerInitializer()
