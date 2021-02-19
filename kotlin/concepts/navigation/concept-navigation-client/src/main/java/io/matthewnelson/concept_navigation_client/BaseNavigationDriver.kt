package io.matthewnelson.concept_navigation_client

abstract class BaseNavigationDriver<T> {
    abstract suspend fun submitNavigationRequest(request: NavigationRequest<T>)
}
