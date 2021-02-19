package io.matthewnelson.feature_authentication_core

import app.cash.exhaustive.Exhaustive
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse
import io.matthewnelson.feature_authentication_core.model.AuthenticationState
import io.matthewnelson.feature_authentication_core.model.ForegroundState
import io.matthewnelson.feature_authentication_core.model.PinEntry
import io.matthewnelson.concept_authentication.AuthenticationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extend [io.matthewnelson.feature_authentication_core.components.AuthenticationManagerImpl]
 * when implementing, not this class.
 *
 * Inject this class as needed.
 * */
abstract class AuthenticationManager {

    abstract fun authenticate(
        pinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse>

    abstract fun resetPin(
        resetPin: AuthenticateFlowResponse.ConfirmNewPinEntryToReset,
        confirmedPinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse>

    abstract fun setPinFirstTime(
        setPinFirstTime: AuthenticateFlowResponse.ConfirmPinEntryToSetForFirstTime,
        confirmedPinEntry: PinEntry,
        requests: List<AuthenticationRequest>
    ): Flow<AuthenticateFlowResponse>

    companion object {
        @Suppress("ObjectPropertyName", "RemoveExplicitTypeArguments")
        private val _authenticationStateFlow: MutableStateFlow<AuthenticationState> by lazy {
            MutableStateFlow<AuthenticationState>(AuthenticationState.Required.InitialLogIn)
        }

        val authenticationStateFlow: StateFlow<AuthenticationState>
            get() = _authenticationStateFlow.asStateFlow()

        @Suppress("ObjectPropertyName", "RemoveExplicitTypeArguments")
        private val _foregroundStateFlow: MutableStateFlow<ForegroundState> by lazy {
            MutableStateFlow<ForegroundState>(ForegroundState.Background)
        }

        val foregroundStateFlow: StateFlow<ForegroundState>
            get() = _foregroundStateFlow.asStateFlow()
    }

    @JvmSynthetic
    internal abstract fun clearEncryptionKey()

    @JvmSynthetic
    @Suppress("UNUSED_PARAMETER")
    internal fun updateAuthenticationState(state: AuthenticationState, any: Any?) {
        @Exhaustive
        when (state) {
            is AuthenticationState.NotRequired -> {}
            is AuthenticationState.Required -> {
                clearEncryptionKey()
            }
        }
        _authenticationStateFlow.value = state
    }

    protected fun updateAuthenticationState(state: AuthenticationState) {
        updateAuthenticationState(state, null)
    }

    protected fun updateForegroundState(state: ForegroundState) {
        _foregroundStateFlow.value = state
    }
}
