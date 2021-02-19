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
package io.matthewnelson.android_feature_views.navigation

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.matthewnelson.android_feature_toast_utils.ToastUtils
import io.matthewnelson.android_feature_toast_utils.ToastUtilsResponse
import io.matthewnelson.android_feature_toast_utils.show
import io.opensolutions.common_views_android.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class CloseAppOnBackPress(
    private val context: Context,
    enable: Boolean = true
): OnBackPressedCallback(enable) {

    private var lastToast: Toast? = null
    private var doubleTapCloseJob: Job? = null
    @Suppress("RemoveExplicitTypeArguments")
    private val backPressedStateFlow: MutableStateFlow<Boolean> by lazy(LazyThreadSafetyMode.NONE) {
        MutableStateFlow<Boolean>(false)
    }

    fun enableDoubleTapToClose(
        owner: LifecycleOwner,
        toastUtils: ToastUtils?,
        @StringRes message: Int = R.string.close_app_double_tap_toast_msg,
        delayTime: Long = 2_000L
    ): CloseAppOnBackPress {
        if (doubleTapCloseJob?.isActive != true) {
            doubleTapCloseJob = owner.lifecycleScope.launch {
                backPressedStateFlow.asStateFlow().collect { backPressed ->
                    if (backPressed) {
                        toastUtils?.show(context, message).let { response ->
                            if (response is ToastUtilsResponse.Success) {
                                lastToast = response.toast
                            }
                        }
                        delay(delayTime)
                        lastToast?.cancel()
                        lastToast = null
                        backPressedStateFlow.value = false
                    }
                }
            }
        }
        return this
    }

    fun addCallback(owner: LifecycleOwner, activity: FragmentActivity) {
        activity.apply {
            onBackPressedDispatcher.addCallback(owner, this@CloseAppOnBackPress)
        }
    }

    override fun handleOnBackPressed() {
        if (doubleTapCloseJob?.isActive == true) {
            if (backPressedStateFlow.value) {
                closeApp()
            } else {
                backPressedStateFlow.value = true
            }
        } else {
            closeApp()
        }
    }

    private fun closeApp() {
        lastToast?.cancel()
        lastToast = null
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(context, intent, null)
    }
}
