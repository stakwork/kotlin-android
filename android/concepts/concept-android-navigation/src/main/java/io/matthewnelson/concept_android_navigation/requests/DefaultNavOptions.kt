package io.matthewnelson.concept_android_navigation.requests

import androidx.navigation.NavOptions
import io.opensolutions.common_navigation_android.R

object DefaultNavOptions {

    @Suppress("SpellCheckingInspection")
    val defaultAnims: NavOptions.Builder
        get() = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_right)
            .setPopExitAnim(R.anim.slide_out_right)

    @Suppress("SpellCheckingInspection")
    val defaultAnimsBuilt: NavOptions by lazy {
        defaultAnims.build()
    }
}
