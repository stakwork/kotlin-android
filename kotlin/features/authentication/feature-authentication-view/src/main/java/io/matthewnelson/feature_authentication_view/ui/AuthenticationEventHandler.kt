package io.matthewnelson.feature_authentication_view.ui

abstract class AuthenticationEventHandler {
    abstract suspend fun onNewPinDoesNotMatchConfirmedPin()
    abstract suspend fun onOneMoreAttemptUntilLockout()
    abstract suspend fun onPinDoesNotMatch()
    abstract suspend fun onWrongPin()
    abstract suspend fun produceHapticFeedback()
}