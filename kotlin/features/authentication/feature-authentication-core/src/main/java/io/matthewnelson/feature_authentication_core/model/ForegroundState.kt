package io.matthewnelson.feature_authentication_core.model

sealed class ForegroundState {
    object Background: ForegroundState()
    object Foreground: ForegroundState()
}
