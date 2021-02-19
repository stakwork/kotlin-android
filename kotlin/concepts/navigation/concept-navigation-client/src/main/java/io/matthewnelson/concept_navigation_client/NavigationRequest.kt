package io.matthewnelson.concept_navigation_client

/**
 * Extend and create a request specific to the destination desired.
 *
 * @sample [io.opensolutions.android_feature_authentication.navigation.AuthenticationNavigationRequest]
 * @sample [io.opensolutions.android_feature_tor_views.navigation.TorNavigationRequest]
 * */
abstract class NavigationRequest<T> {
    abstract fun navigate(controller: T)
}
