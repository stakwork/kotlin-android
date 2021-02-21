package io.matthewnelson.concept_foreground_state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class ForegroundStateManager {

    @Suppress("ObjectPropertyName", "RemoveExplicitTypeArguments")
    private val _foregroundStateFlow: MutableStateFlow<ForegroundState> by lazy {
        MutableStateFlow<ForegroundState>(ForegroundState.Background)
    }

    val foregroundStateFlow: StateFlow<ForegroundState>
        get() = _foregroundStateFlow.asStateFlow()

    protected open fun updateForegroundState(state: ForegroundState) {
        _foregroundStateFlow.value = state
    }
}