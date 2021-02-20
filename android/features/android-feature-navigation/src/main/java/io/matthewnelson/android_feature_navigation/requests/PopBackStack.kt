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
package io.matthewnelson.android_feature_navigation.requests

import androidx.annotation.IdRes
import androidx.navigation.NavController
import io.matthewnelson.concept_navigation.NavigationRequest

class PopBackStack(
    @IdRes private val destinationId: Int? = null,
    private val inclusive: Boolean = false
): NavigationRequest<NavController>() {
    override fun navigate(controller: NavController) {
        destinationId?.let { dest ->
            controller.popBackStack(dest, inclusive)
        } ?: controller.previousBackStackEntry?.let {
            controller.popBackStack()
        }
    }
}
