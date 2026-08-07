package com.azkry.app.features.home.models

import com.azkry.app.core.models.DhikrCategoryKeys
import com.azkry.app.core.models.Prayer
import java.time.Duration
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDayViewTest {
    private val times = mapOf(
        Prayer.Fajr to LocalTime.of(4, 15),
        Prayer.Sunrise to LocalTime.of(6, 7),
        Prayer.Dhuhr to LocalTime.of(13, 45),
        Prayer.Asr to LocalTime.of(17, 30),
        Prayer.Maghrib to LocalTime.of(21, 10),
        Prayer.Isha to LocalTime.of(22, 40),
    )

    @Test
    fun `phases follow the day's prayer events`() {
        assertEquals(HeaderPhase.Night, HomeDayView.phaseFor(LocalTime.of(2, 0), times))
        assertEquals(HeaderPhase.Dawn, HomeDayView.phaseFor(LocalTime.of(5, 0), times))
        assertEquals(HeaderPhase.Day, HomeDayView.phaseFor(LocalTime.of(12, 30), times))
        assertEquals(HeaderPhase.Afternoon, HomeDayView.phaseFor(LocalTime.of(18, 0), times))
        assertEquals(HeaderPhase.Dusk, HomeDayView.phaseFor(LocalTime.of(21, 30), times))
        assertEquals(HeaderPhase.Night, HomeDayView.phaseFor(LocalTime.of(23, 0), times))
    }

    @Test
    fun `strip frames now between the passed and upcoming events`() {
        // Midday: sunrise has passed, dhuhr is next (worked example).
        assertEquals(
            Prayer.Sunrise to Prayer.Dhuhr,
            HomeDayView.stripEvents(LocalTime.of(12, 30), times),
        )
        // Evening between maghrib and isha.
        assertEquals(
            Prayer.Maghrib to Prayer.Isha,
            HomeDayView.stripEvents(LocalTime.of(21, 30), times),
        )
        // After isha and before fajr the frame wraps.
        assertEquals(
            Prayer.Isha to Prayer.Fajr,
            HomeDayView.stripEvents(LocalTime.of(23, 30), times),
        )
        assertEquals(
            Prayer.Isha to Prayer.Fajr,
            HomeDayView.stripEvents(LocalTime.of(3, 0), times),
        )
    }

    @Test
    fun `suggested adhkar follow the phase`() {
        assertEquals(DhikrCategoryKeys.MORNING, HomeDayView.suggestedAdhkarKey(HeaderPhase.Day))
        assertEquals(DhikrCategoryKeys.EVENING, HomeDayView.suggestedAdhkarKey(HeaderPhase.Dusk))
        assertEquals(DhikrCategoryKeys.SLEEP, HomeDayView.suggestedAdhkarKey(HeaderPhase.Night))
    }

    @Test
    fun `countdown phrase rounds minutes up with arabic plural rules`() {
        // 3:08 left reads "4 دقائق" as the design specifies.
        assertEquals("4 دقائق", HomeDayView.countdownPhrase(Duration.ofSeconds(3 * 60 + 8)))
        assertEquals("أقل من دقيقة", HomeDayView.countdownPhrase(Duration.ofSeconds(45)))
        assertEquals("دقيقة", HomeDayView.countdownPhrase(Duration.ofSeconds(60)))
        assertEquals("دقيقتين", HomeDayView.countdownPhrase(Duration.ofSeconds(2 * 60)))
        assertEquals("59 دقيقة", HomeDayView.countdownPhrase(Duration.ofMinutes(59)))
        assertEquals("ساعة", HomeDayView.countdownPhrase(Duration.ofHours(1)))
        assertEquals("ساعتين", HomeDayView.countdownPhrase(Duration.ofHours(2)))
        assertEquals("5 ساعات", HomeDayView.countdownPhrase(Duration.ofHours(5)))
        assertEquals(
            "ساعة و57 دقيقة",
            HomeDayView.countdownPhrase(Duration.ofSeconds(1 * 3600 + 56 * 60 + 10)),
        )
        assertEquals("أقل من دقيقة", HomeDayView.countdownPhrase(Duration.ofSeconds(-5)))
    }

    @Test
    fun `countdown clock drops the hour field below one hour`() {
        assertEquals("3:08", HomeDayView.countdownClock(Duration.ofSeconds(3 * 60 + 8)))
        assertEquals("0:45", HomeDayView.countdownClock(Duration.ofSeconds(45)))
        assertEquals(
            "1:56:10",
            HomeDayView.countdownClock(Duration.ofSeconds(1 * 3600 + 56 * 60 + 10)),
        )
        assertEquals("0:00", HomeDayView.countdownClock(Duration.ofSeconds(-5)))
    }
}
