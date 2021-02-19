package io.matthewnelson.feature_authentication_core.components

/**
 * Wrong Pin Lockout feature will be enabled **only** if both
 * [wrongPinAttemptsUntilLockedOut] and [wrongPinLockoutDuration] are
 * greater than 0.
 * */
abstract class AuthenticationManagerInitializer {
    abstract val wrongPinAttemptsUntilLockedOut: Int
    abstract val wrongPinLockoutDuration: Long
}
