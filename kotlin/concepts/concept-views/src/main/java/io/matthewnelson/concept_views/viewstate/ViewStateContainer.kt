package io.matthewnelson.concept_views.viewstate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

suspend inline fun <VS: ViewState<VS>> ViewStateContainer<VS>.collect(
    crossinline action: suspend (value: VS) -> Unit
): Unit =
    this.viewStateFlow.collect { action(it) }

inline val <VS: ViewState<VS>>ViewStateContainer<VS>.value: VS
    get() = this.viewStateFlow.value

open class ViewStateContainer<VS: ViewState<VS>>(initialViewState: VS) {
    @Suppress("PropertyName", "RemoveExplicitTypeArguments")
    protected val _viewStateFlow: MutableStateFlow<VS> by lazy {
        MutableStateFlow<VS>(initialViewState)
    }

    open val viewStateFlow: StateFlow<VS>
        get() = _viewStateFlow.asStateFlow()

    open fun updateViewState(viewState: VS) {
        _viewStateFlow.value = viewState
    }
}
