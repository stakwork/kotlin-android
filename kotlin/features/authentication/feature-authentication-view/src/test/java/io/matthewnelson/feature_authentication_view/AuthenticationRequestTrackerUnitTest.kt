package io.matthewnelson.feature_authentication_view

import io.matthewnelson.concept_authentication.AuthenticationRequest
import io.matthewnelson.feature_authentication_view.components.AuthenticationRequestTracker
import org.junit.Assert
import org.junit.Test

internal class AuthenticationRequestTrackerUnitTest {

    private val tracker = AuthenticationRequestTracker()

    @Test
    fun `adding of requests sorts by request priority and returns true if highest priority changed`() {
        Assert.assertTrue(tracker.getRequestsList().isEmpty())
        Assert.assertTrue(tracker.addRequest(AuthenticationRequest.GetEncryptionKey()))
        Assert.assertTrue(tracker.addRequest(AuthenticationRequest.LogIn()))
        Assert.assertFalse(tracker.addRequest(AuthenticationRequest.GetEncryptionKey()))
        Assert.assertFalse(tracker.addRequest(AuthenticationRequest.ResetPin()))
        Assert.assertFalse(tracker.addRequest(AuthenticationRequest.GetEncryptionKey()))
        Assert.assertTrue(tracker.addRequest(AuthenticationRequest.LogIn()))
    }
}
