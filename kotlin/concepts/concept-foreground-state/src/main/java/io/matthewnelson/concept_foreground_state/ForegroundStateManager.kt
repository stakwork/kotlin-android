package io.matthewnelson.concept_foreground_state

import kotlinx.coroutines.flow.StateFlow

abstract class ForegroundStateManager {
    abstract val foregroundStateFlow: StateFlow<ForegroundState>
}