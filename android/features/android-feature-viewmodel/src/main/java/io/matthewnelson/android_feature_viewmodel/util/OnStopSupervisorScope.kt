package io.matthewnelson.android_feature_viewmodel.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OnStopSupervisorScope(owner: LifecycleOwner): DefaultLifecycleObserver {
    private var supervisor = SupervisorJob()
    private var scope = CoroutineScope(supervisor)

    fun scope(): CoroutineScope =
        scope

    override fun onStop(owner: LifecycleOwner) {
        supervisor.cancel()
        supervisor = SupervisorJob().also {
            scope = CoroutineScope(it)
        }
        super.onStop(owner)
    }

    init {
        owner.lifecycle.addObserver(this)
    }
}
