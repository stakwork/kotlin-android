package io.matthewnelson.android_feature_views.ui.motionlayout

import androidx.annotation.IdRes
import androidx.constraintlayout.motion.widget.MotionLayout
import io.matthewnelson.concept_views.viewstate.ViewState

/**
 * An abstraction of [ViewState] for use in a [MotionLayout].
 *
 * For the final state of the Motion Scene, define [endSetId] as null, and override
 * [restoreMotionScene] to implement the transition and progress.
 *
 * @see [MotionLayoutFragment]
 * @see [MotionLayoutViewModel]
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
     *
     * @see [MotionLayoutFragment.onViewStateFlowCollect]
     * */
    open fun transitionToEndSet(motionLayout: MotionLayout) {
        endSetId?.let { endSet ->
            motionLayout.transitionToState(endSet)
        }
    }

    /**
     * Default behavior is to set the Motion Scene to it's starting state. Override to customize.
     *
     * @see [MotionLayoutFragment.onViewCreatedRestoreMotionScene]
     * */
    open fun restoreMotionScene(motionLayout: MotionLayout) {
        endSetId?.let { endSet ->
            motionLayout.setTransition(startSetId, endSet)
            motionLayout.setProgress(0F, 0F)
        }
    }
}
