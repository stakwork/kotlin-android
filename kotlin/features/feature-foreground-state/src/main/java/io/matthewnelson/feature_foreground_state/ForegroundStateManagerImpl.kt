package io.matthewnelson.feature_foreground_state

import io.matthewnelson.concept_foreground_state.ForegroundState
import io.matthewnelson.concept_foreground_state.ForegroundStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ForegroundStateManagerImpl: ForegroundStateManager() {

    @Suppress("ObjectPropertyName", "RemoveExplicitTypeArguments")
    private val _foregroundStateFlow: MutableStateFlow<ForegroundState> by lazy {
        MutableStateFlow<ForegroundState>(ForegroundState.Background)
    }

    override val foregroundStateFlow: StateFlow<ForegroundState>
        get() = _foregroundStateFlow.asStateFlow()

    protected open fun updateForegroundState(state: ForegroundState) {
        _foregroundStateFlow.value = state
    }
}