package io.matthewnelson.feature_authentication_view.navigation

import io.matthewnelson.concept_navigation_client.BaseNavigationDriver
import io.matthewnelson.concept_navigation_client.Navigator

abstract class AuthenticationNavigator<T>(
    navigationDriver: BaseNavigationDriver<T>
): Navigator<T>(navigationDriver) {
    abstract suspend fun toAuthenticationView()
    abstract suspend fun popBackStack()
}