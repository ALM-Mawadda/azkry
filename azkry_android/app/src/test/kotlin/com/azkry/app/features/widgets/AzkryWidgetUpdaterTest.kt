package com.azkry.app.features.widgets

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AzkryWidgetUpdaterTest {
    @Test
    fun `logs every non-cancellation failure and continues updating`() = runTest {
        val nextPrayerError = IllegalStateException("next prayer failed")
        val dailyDhikrError = IllegalArgumentException("daily dhikr failed")
        var nextPrayerCalled = false
        var dailyDhikrCalled = false
        val failures = mutableListOf<Pair<String, Throwable>>()
        val updater = AzkryWidgetUpdater(
            updateNextPrayer = {
                nextPrayerCalled = true
                throw nextPrayerError
            },
            updateDailyDhikr = {
                dailyDhikrCalled = true
                throw dailyDhikrError
            },
            logFailure = { name, error -> failures += name to error },
        )

        updater.updateAll()

        assertTrue(nextPrayerCalled)
        assertTrue(dailyDhikrCalled)
        assertEquals(
            listOf(
                "NextPrayerWidget" to nextPrayerError,
                "DailyDhikrWidget" to dailyDhikrError,
            ),
            failures,
        )
    }

    @Test
    fun `rethrows cancellation and stops subsequent updates`() = runTest {
        val cancellation = CancellationException("cancel widgets")
        var dailyDhikrCalled = false
        val failures = mutableListOf<Pair<String, Throwable>>()
        val updater = AzkryWidgetUpdater(
            updateNextPrayer = { throw cancellation },
            updateDailyDhikr = { dailyDhikrCalled = true },
            logFailure = { name, error -> failures += name to error },
        )

        try {
            updater.updateAll()
            fail("Expected cancellation to be rethrown")
        } catch (error: CancellationException) {
            assertSame(cancellation, error)
        }

        assertFalse(dailyDhikrCalled)
        assertTrue(failures.isEmpty())
    }
}
