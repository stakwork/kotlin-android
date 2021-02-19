package io.matthewnelson.android_feature_views.ui.motionlayout

import io.matthewnelson.android_feature_views.ui.base.BaseViewModel
import io.matthewnelson.android_feature_views.ui.base.BaseFragment
import io.matthewnelson.concept_views.sideeffect.SideEffect
import io.matthewnelson.android_feature_views.ui.sideeffect.SideEffectFragment
import io.matthewnelson.android_feature_views.ui.sideeffect.SideEffectViewModel

/**
 * An abstraction that defines the [BaseViewModel.viewStateContainer] as being of type
 * [MotionLayoutViewState].
 *
 * @see [MotionLayoutFragment]
 * @see [MotionLayoutViewState]
 * */
abstract class MotionLayoutViewModel<
        MSC: Any,
        T,
        SE: SideEffect<T>,
        MLVS: MotionLayoutViewState<MLVS>
        >(initialViewState: MLVS): SideEffectViewModel<
        T,
        SE,
        MLVS
        >(initialViewState)
{

    /**
     * Best if called from the Fragment/Activity
     *
     * Example:
     *
     *   lifecycleScope.launchWhenStarted {
     *       viewModel.viewStateContainer.collect { myMotionLayoutViewState ->
     *           if (myMotionLayoutViewState is MyMotionLayoutViewState.FinalState) {
     *               viewModel.onMotionSceneCompletion()
     *           }
     *       }
     *   }
     *
     * Alternatively (if using a [BaseFragment], [SideEffectFragment], or [MotionLayoutFragment]):
     *
     *   onViewStateFlowCollect(viewState: MyMotionLayoutViewState) {
     *       if (viewState is MyMotionLayoutViewState.FinalState) {
     *           viewModel.onMotionSceneCompletion()
     *       }
     *   }
     * */
    abstract suspend fun onMotionSceneCompletion(value: MSC)
}
