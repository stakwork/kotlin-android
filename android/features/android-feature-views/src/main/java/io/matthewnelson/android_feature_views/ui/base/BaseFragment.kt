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
package io.matthewnelson.android_feature_views.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import io.matthewnelson.concept_views.viewstate.ViewState

abstract class BaseFragment<
        VS: ViewState<VS>,
        BVM: BaseViewModel<VS>,
        VB: ViewBinding
        >(@LayoutRes layoutId: Int): Fragment(layoutId)
{
    protected abstract val viewModel: BVM
    protected abstract val binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToViewStateFlow()
    }

    protected abstract suspend fun onViewStateFlowCollect(viewState: VS)

    /**
     * Called from [onCreate]. Must be mindful if overriding to lazily start things
     * using lifecycleScope.launchWhenStarted
     * */
    protected open fun subscribeToViewStateFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.collectViewState { viewState ->
                onViewStateFlowCollect(viewState)
            }
        }
    }
}
