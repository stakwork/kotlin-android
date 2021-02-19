package io.matthewnelson.test_concept_coroutines

import io.matthewnelson.concept_coroutines.CoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

abstract class CoroutineTestHelper {

    protected val testDispatcher = TestCoroutineDispatcher()

    private class TestCoroutineDispatchers(
        default: CoroutineDispatcher,
        io: CoroutineDispatcher,
        main: CoroutineDispatcher,
        mainImmediate: CoroutineDispatcher,
        unconfined: CoroutineDispatcher
    ): CoroutineDispatchers(default, io, main, mainImmediate, unconfined)

    protected val dispatchers: CoroutineDispatchers by lazy {
        TestCoroutineDispatchers(
            testDispatcher,
            testDispatcher,
            testDispatcher,
            testDispatcher,
            testDispatcher
        )
    }

    /**
     * Call from @Before to set application wide dispatchers to that of your test
     *
     * can send it a dispatcher if you don't want to use [testDispatcher] as the
     * default.
     * */
    protected fun setupCoroutineTestHelper() {
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Call from @After if using the [testDispatcher]
     * */
    protected fun tearDownCoroutineTestHelper() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}
