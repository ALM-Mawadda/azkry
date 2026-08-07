package com.azkry.app.features.prayertimes.models

import com.azkry.app.core.models.Prayer
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrayerDayViewTest {
    // The worked example day: Fajr 4:19, Sunrise 6:08, Dhuhr 13:45,
    // Asr 17:51, Maghrib 21:22, Isha 23:11.
    private val times = mapOf(
        Prayer.Fajr to LocalTime.of(4, 19),
        Prayer.Sunrise to LocalTime.of(6, 8),
        Prayer.Dhuhr to LocalTime.of(13, 45),
        Prayer.Asr to LocalTime.of(17, 51),
        Prayer.Maghrib to LocalTime.of(21, 22),
        Prayer.Isha to LocalTime.of(23, 11),
    )

    @Test
    fun `ishraq starts exactly when the after-fajr window closes`() {
        val sunrise = times.getValue(Prayer.Sunrise)
        val ishraq = PrayerDayView.ishraqTime(sunrise)

        assertEquals(LocalTime.of(6, 23), ishraq)
        // One basis, one constant: the window must close at that same instant.
        assertEquals(
            ForbiddenWindow.AfterFajr,
            PrayerDayView.forbiddenWindow(ishraq.minusMinutes(1), times),
        )
        assertNull(PrayerDayView.forbiddenWindow(ishraq, times))
    }

    @Test
    fun `after asr window covers the sample moment`() {
        // 19:35 in the screenshot sits between Asr and Maghrib.
        assertEquals(
            ForbiddenWindow.AfterAsr,
            PrayerDayView.forbiddenWindow(LocalTime.of(19, 35), times),
        )
        assertEquals(
            ForbiddenWindow.AfterAsr,
            PrayerDayView.forbiddenWindow(LocalTime.of(17, 51), times),
        )
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(21, 22), times))
    }

    @Test
    fun `after fajr window ends once the sun has cleared the horizon`() {
        assertEquals(
            ForbiddenWindow.AfterFajr,
            PrayerDayView.forbiddenWindow(LocalTime.of(4, 19), times),
        )
        assertEquals(
            ForbiddenWindow.AfterFajr,
            PrayerDayView.forbiddenWindow(LocalTime.of(6, 20), times),
        )
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(6, 23), times))
    }

    @Test
    fun `zawal window opens shortly before dhuhr and closes at the adhan`() {
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(13, 34), times))
        assertEquals(
            ForbiddenWindow.Zawal,
            PrayerDayView.forbiddenWindow(LocalTime.of(13, 40), times),
        )
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(13, 45), times))
    }

    @Test
    fun `prayer is allowed outside the three windows`() {
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(3, 0), times))
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(10, 0), times))
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(23, 30), times))
    }

    @Test
    fun `missing times never report a forbidden window`() {
        assertNull(PrayerDayView.forbiddenWindow(LocalTime.of(19, 35), emptyMap()))
        assertNull(
            PrayerDayView.forbiddenWindow(
                LocalTime.of(19, 35),
                times - Prayer.Maghrib,
            ),
        )
    }
}
