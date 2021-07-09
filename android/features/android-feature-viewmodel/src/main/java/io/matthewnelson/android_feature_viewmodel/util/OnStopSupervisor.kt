/*
*  Copyright 2021 Matthew Nelson
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* */
package io.matthewnelson.android_feature_viewmodel.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OnStopSupervisor: DefaultLifecycleObserver {
    private var supervisor = SupervisorJob()
    var scope = CoroutineScope(supervisor)
        private set

    override fun onStop(owner: LifecycleOwner) {
        supervisor.cancel()
        supervisor = SupervisorJob().also {
            scope = CoroutineScope(it)
        }
        super.onStop(owner)
    }

    /**
     * Call from fragment's onViewCreated
     * */
    fun observe(owner: LifecycleOwner): OnStopSupervisor {
        owner.lifecycle.addObserver(this)
        return this
    }
}
