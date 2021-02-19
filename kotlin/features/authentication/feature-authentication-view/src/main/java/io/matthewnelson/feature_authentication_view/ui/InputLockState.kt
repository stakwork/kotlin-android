package io.matthewnelson.feature_authentication_view.ui

import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse

sealed class InputLockState {
    abstract val show: Boolean
    open val flowResponseNotify: AuthenticateFlowResponse.Notify? = null

    object Unlocked: InputLockState() {
        override val show: Boolean
            get() = false
    }

    sealed class Locked: InputLockState() {
        override val show: Boolean
            get() = true

        object Idle: Locked()
        class Notify(override val flowResponseNotify: AuthenticateFlowResponse.Notify): Locked()
    }
}