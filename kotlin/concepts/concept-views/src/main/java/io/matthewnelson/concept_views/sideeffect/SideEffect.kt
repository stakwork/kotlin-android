package io.matthewnelson.concept_views.sideeffect

/**
 * A [SideEffect] of a View is something that is a by product of it's State.
 * */
abstract class SideEffect<T> {
    abstract suspend fun execute(value: T)
}
