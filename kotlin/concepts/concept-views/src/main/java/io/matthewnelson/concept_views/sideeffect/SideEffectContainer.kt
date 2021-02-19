package io.matthewnelson.concept_views.sideeffect

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive

suspend inline fun <T, SE: SideEffect<T>> SideEffectContainer<T, SE>.collect(
    crossinline action: suspend (value: SE) -> Unit
): Unit =
    this.sideEffectFlow.collect { action(it) }

open class SideEffectContainer<T, SE: SideEffect<T>>(val submissionSuspendTimeOut: Long = 100L) {

    init {
        require(submissionSuspendTimeOut > 0L) { "submissionTimeOut must be greater than 0L" }
    }

    @Suppress("PropertyName", "RemoveExplicitTypeArguments")
    protected val _sideEffectFlow: MutableSharedFlow<SE> by lazy {
        MutableSharedFlow<SE>(0, 1)
    }

    open val sideEffectFlow: SharedFlow<SE>
        get() = _sideEffectFlow.asSharedFlow()

    open suspend fun submitSideEffect(sideEffect: SE) {
        var timeout = 0L
        while (
            currentCoroutineContext().isActive &&
            _sideEffectFlow.subscriptionCount.value == 0 &&
            timeout < submissionSuspendTimeOut
        ) {
            delay(25L)
            timeout += 25L
        }
        _sideEffectFlow.emit(sideEffect)
    }
}
