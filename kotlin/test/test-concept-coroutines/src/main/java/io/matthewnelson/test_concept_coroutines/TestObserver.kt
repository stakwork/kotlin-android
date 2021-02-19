package io.matthewnelson.test_concept_coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.junit.Assert

fun <F> Flow<F>.testObserver(scope: CoroutineScope): TestObserver<F> =
    TestObserver(scope, this)

class TestObserver<T>(scope: CoroutineScope, flow: Flow<T>) {
    val values = mutableListOf<T>()
    private val job: Job = scope.launch {
        flow.collect { values.add(it) }
    }
    fun assertNoValues(): TestObserver<T> {
        Assert.assertEquals(emptyList<T>(), this.values)
        return this
    }
    suspend fun delay10(): TestObserver<T> {
        delay(10L)
        return this
    }
    fun assertValues(vararg values: T): TestObserver<T> {
        Assert.assertEquals(values.toList(), this.values)
        return this
    }
    fun finish() {
        job.cancel()
    }
}
