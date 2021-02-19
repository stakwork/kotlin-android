package io.matthewnelson.concept_coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * [kotlinx.coroutines.MainCoroutineDispatcher.immediate] is not supported on some
 * platforms. If that is the case, when extending [CoroutineDispatcher] initialize
 * [mainImmediate] with [kotlinx.coroutines.Dispatchers.Main]
 * */
abstract class CoroutineDispatchers(
    val default: CoroutineDispatcher,
    val io: CoroutineDispatcher,
    val main: CoroutineDispatcher,
    val mainImmediate: CoroutineDispatcher,
    val unconfined: CoroutineDispatcher
)
