package io.matthewnelson.test_feature_authentication_core

import org.junit.After
import org.junit.Before

/**
 * Simple utility class to initialize the test helper with defaults instead and setup things
 * */
abstract class AuthenticationCoreDefaultsTestHelper: AuthenticationCoreTestHelper<
        TestAuthenticationManagerInitializer,
        TestEncryptionKeyHandler,
        TestAuthenticationCoreStorage
        >()
{
    override val testInitializer: TestAuthenticationManagerInitializer =
        TestAuthenticationManagerInitializer()
    override val testHandler: TestEncryptionKeyHandler =
        TestEncryptionKeyHandler()
    override val testStorage: TestAuthenticationCoreStorage =
        TestAuthenticationCoreStorage()

    @Before
    fun setup() {
        setupAuthenticationCoreTestHelper()
    }

    @After
    fun tearDown() {
        tearDownAuthenticationCoreTestHelper()
    }
}