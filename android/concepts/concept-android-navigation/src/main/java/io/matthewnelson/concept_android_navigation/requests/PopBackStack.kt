package io.matthewnelson.concept_android_navigation.requests

import androidx.annotation.IdRes
import androidx.navigation.NavController
import io.matthewnelson.concept_navigation_client.NavigationRequest

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
