package io.matthewnelson.feature_authentication_view.components

import io.matthewnelson.concept_authentication.AuthenticationRequest

internal class AuthenticationRequestTracker {

    private val requests = mutableListOf<AuthenticationRequest>()
    @Volatile
    private var highestPriorityRequest = 100

    /**
     * Adds the [request] to the list. If the [AuthenticationRequest.priority],
     * is lower than the priority level of the last index, it will sort the list.
     *
     * @return true if priority was changed, false if not
     * */
    @JvmSynthetic
    @Synchronized
    fun addRequest(request: AuthenticationRequest): Boolean {
        requests.add(request)
        return if (request.priority <= highestPriorityRequest) {
            highestPriorityRequest = request.priority
            true
        } else {
            false
        }
    }

    @JvmSynthetic
    @Synchronized
    fun getRequestListSize(): Int =
        requests.size

    @JvmSynthetic
    @Synchronized
    fun getRequestsList(): List<AuthenticationRequest> =
        requests.toList()
}
