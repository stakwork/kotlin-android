package io.matthewnelson.concept_navigation_client

/**
 * Implement a single [Navigator] for each module, then define any abstract methods
 * in the Application (which is connected to everything). This allows for modules to
 * be completely decoupled while ensuring that arguments are passed if need be (by
 * requiring an argument in the [NavigationRequest]'s constructor, and implementing
 * the passing of said argument via [NavigationRequest.navigate]'s execution.
 *
 * @sample [io.opensolutions.home.navigation.HomeDrawerNavigator]
 * @sample [io.opensolutions.toxicity.navigation.HomeDrawerNavigatorImpl]
 * */
abstract class Navigator<T>(
    protected val navigationDriver: BaseNavigationDriver<T>
)
