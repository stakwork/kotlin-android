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
package io.matthewnelson.android_concept_views

import androidx.annotation.IdRes
import androidx.constraintlayout.motion.widget.MotionLayout
import io.matthewnelson.concept_views.viewstate.ViewState

/**
 * An abstraction of [ViewState] for use in a [MotionLayout].
 *
 * For the final state of the Motion Scene, define [endSetId] as null, and override
 * [restoreMotionScene] to implement the transition and progress.
 * */
abstract class MotionLayoutViewState<MLVS>: ViewState<MLVS>() {

    @get:IdRes
    abstract val startSetId: Int

    /**
     * Define as null for the final state of the [MotionLayoutViewState]
     * */
    @get:IdRes
    abstract val endSetId: Int?

    /**
     * Transitions the Motion Scene to it's defined [endSetId]. Override to customize.
     * */
    open fun transitionToEndSet(motionLayout: MotionLayout) {
        endSetId?.let { endSet ->
            motionLayout.transitionToState(endSet)
        }
    }

    /**
     * Default behavior is to set the Motion Scene to it's starting state. Override to customize.
     * */
    open fun restoreMotionScene(motionLayout: MotionLayout) {
        endSetId?.let { endSet ->
            motionLayout.setTransition(startSetId, endSet)
            motionLayout.setProgress(0F, 0F)
        }
    }
}
