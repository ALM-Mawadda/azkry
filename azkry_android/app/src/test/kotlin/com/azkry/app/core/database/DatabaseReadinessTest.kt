package com.azkry.app.core.database

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DatabaseReadinessTest {
    @Test
    fun `awaitReady waits for owned initialization`() = runTest {
        val release = CompletableDeferred<Unit>()
        val readiness = DatabaseReadiness(backgroundScope)
        readiness.initialize { release.await() }

        val waiting = async { readiness.awaitReady() }
        assertFalse(waiting.isCompleted)

        release.complete(Unit)
        waiting.await()
    }

    @Test
    fun `awaitReady exposes initialization failure`() = runTest {
        val failure = IOException("seed failed")
        val readiness = DatabaseReadiness(backgroundScope)
        readiness.initialize { throw failure }

        val received = runCatching { readiness.awaitReady() }.exceptionOrNull()

        assertEquals(IOException::class, received?.javaClass?.kotlin)
        assertEquals(failure.message, received?.message)
    }
}
