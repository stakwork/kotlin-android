package io.matthewnelson.android_feature_views.ui.sideeffect

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import io.matthewnelson.concept_views.sideeffect.SideEffect
import io.matthewnelson.concept_views.viewstate.ViewState
import io.matthewnelson.android_feature_views.ui.base.BaseFragment

/**
 * An abstraction that adds [SideEffect]s to the [BaseFragment].
 *
 * @see [SideEffect]
 * @see [SideEffectViewModel]
 * */
abstract class SideEffectFragment<
        T,
        SE: SideEffect<T>,
        VS: ViewState<VS>,
        SEVM: SideEffectViewModel<T, SE, VS>,
        VB: ViewBinding
        >(@LayoutRes layoutId: Int): BaseFragment<
        VS,
        SEVM,
        VB
        >(layoutId)
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToSideEffectSharedFlow()
    }

    protected abstract suspend fun onSideEffectCollect(sideEffect: SE)

    /**
     * Called from [onCreate]. Must be mindful if overriding to lazily start things
     * using lifecycleScope.launchWhenStarted
     * */
    protected open fun subscribeToSideEffectSharedFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.collectSideEffects { sideEffect ->
                onSideEffectCollect(sideEffect)
            }
        }
    }
}
