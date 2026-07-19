package com.azkry.app.features.prayertimes.services

import com.google.android.gms.tasks.CancellationToken
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationServiceTest {
    @Test
    fun `cancelling coroutine cancels the Play Services request token`() = runTest {
        lateinit var requestToken: CancellationToken
        val requestStarted = CompletableDeferred<Unit>()

        val job = launch {
            awaitCurrentLocation { token, _ ->
                requestToken = token
                requestStarted.complete(Unit)
            }
        }
        requestStarted.await()
        assertFalse(requestToken.isCancellationRequested)

        job.cancelAndJoin()

        assertTrue(requestToken.isCancellationRequested)
    }
}
