package io.matthewnelson.feature_authentication_core

import io.matthewnelson.concept_authentication.coordinator.AuthenticationRequest
import io.matthewnelson.concept_authentication_core.model.UserInput
import io.matthewnelson.feature_authentication_core.model.AuthenticateFlowResponse
import io.matthewnelson.feature_authentication_core.model.UserInputWriter
import io.matthewnelson.test_feature_authentication_core.AuthenticationCoreDefaultsTestHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

class AuthenticationCoreManagerUnitTest: AuthenticationCoreDefaultsTestHelper() {

    private fun getValidUserInput(
        c: Char,
        repeat: Int = testInitializer.minimumUserInputLength
    ): UserInput {
        val input = testCoreManager.getNewUserInput()
        repeat(repeat) {
            input.addCharacter(c)
        }
        return input
    }

    @Test
    fun `isAnEncryptionKeySet returns correct boolean value if encryption key is in storage`() =
        testDispatcher.runBlockingTest {
            Assert.assertFalse(testCoreManager.isAnEncryptionKeySet())
            login()
            Assert.assertTrue(testCoreManager.isAnEncryptionKeySet())
        }

    @Test
    fun `min user input length not met returns error`() =
        testDispatcher.runBlockingTest {
            val input = getValidUserInput('a')
            input.dropLastCharacter()

            // Authenticate API
            val request = AuthenticationRequest.LogIn(encryptionKey = null)
            testCoreManager.authenticate(input, listOf(request)).first().let { flowResponse ->
                if (flowResponse !is AuthenticateFlowResponse.Error.Authenticate.InvalidPasswordEntrySize) {
                    Assert.fail()
                }
            }

            // SetPasswordFirstTime AIP
            val setPasswordResponse = AuthenticateFlowResponse
                .ConfirmInputToSetForFirstTime.instantiate(input as UserInputWriter)
            testCoreManager.setPasswordFirstTime(setPasswordResponse, input, listOf(request))
                .first().let { flowResponse ->
                    if (flowResponse !is AuthenticateFlowResponse.Error.SetPasswordFirstTime.InvalidNewPasswordEntrySize) {
                        Assert.fail()
                    }
                }

            // ResetPassword API
            val resetRequest = AuthenticationRequest.ResetPassword()
            val resetResponse = AuthenticateFlowResponse
                .PasswordConfirmedForReset.generate(input, resetRequest)!! // won't be on the list
            resetResponse.storeNewPasswordToBeSet(input)
            testCoreManager.resetPassword(resetResponse, input, listOf(resetRequest))
                .first().let { flowResponse ->
                    if (flowResponse !is AuthenticateFlowResponse.Error.ResetPassword.InvalidNewPasswordEntrySize) {
                        Assert.fail()
                    }
                }
        }
}