package com.azkry.app.features.prayertimes.services

import com.azkry.app.core.models.Prayer
import com.azkry.app.core.prayertimes.CalculationMethod
import com.azkry.app.core.prayertimes.GeoLocation
import com.azkry.app.core.prayertimes.PrayerTimeCalculator
import io.mockk.every
import io.mockk.mockk
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PrayerTimesServiceTest {
    @Test
    fun `configured zone determines the calculation offset`() = runTest {
        val date = LocalDate.of(2026, 7, 1)
        val zoneId = ZoneId.of("Pacific/Kiritimati")
        val settings = PrayerSettings(
            cityName = "Kiritimati",
            location = GeoLocation(latitude = 1.8721, longitude = -157.4278),
            zoneId = zoneId,
            method = CalculationMethod.MuslimWorldLeague,
        )
        val service = serviceWith(settings)

        val actual = service.observeTimes(date).first()
        val utcOffsetHours = date.atTime(LocalTime.NOON)
            .atZone(zoneId)
            .offset
            .totalSeconds / 3600.0
        val expected = PrayerTimeCalculator.calculate(
            date = date,
            location = settings.location,
            utcOffsetHours = utcOffsetHours,
            method = settings.method,
            asrMadhab = settings.asrMadhab,
            highLatitudeRule = settings.highLatitudeRule,
        )

        assertEquals(expected, actual.times)
        assertEquals(zoneId, actual.settings.zoneId)
    }

    @Test
    fun `next prayer after isha uses the next date's calculated fajr`() = runTest {
        val date = LocalDate.of(2026, 2, 1)
        val settings = PrayerSettings(
            cityName = "Paris",
            location = GeoLocation(latitude = 48.8566, longitude = 2.3522),
            zoneId = ZoneId.of("Europe/Paris"),
            method = CalculationMethod.France15,
        )
        val service = serviceWith(settings)
        val todayTimes = service.observeTimes(date).first()
        val tomorrowTimes = service.observeTimes(date.plusDays(1)).first()
        val localNow = LocalDateTime.of(date, todayTimes.times[Prayer.Isha].plusMinutes(1))
        val now = localNow.atZone(settings.zoneId).toInstant()

        val next = service.nextPrayer(now, todayTimes)
        val expectedAt = LocalDateTime.of(date.plusDays(1), tomorrowTimes.times[Prayer.Fajr])
            .atZone(settings.zoneId)

        assertNotEquals(todayTimes.times[Prayer.Fajr], tomorrowTimes.times[Prayer.Fajr])
        assertEquals(Prayer.Fajr, next.prayer)
        assertEquals(expectedAt, next.at)
        assertEquals(Duration.between(now, expectedAt.toInstant()), next.remaining)
    }

    @Test
    fun `current times use the configured civil date rather than the device date`() = runTest {
        val settings = PrayerSettings(
            cityName = "Kiritimati",
            location = GeoLocation(latitude = 1.8721, longitude = -157.4278),
            zoneId = ZoneId.of("Pacific/Kiritimati"),
        )
        val service = serviceWith(settings)
        val instant = Instant.parse("2026-07-19T12:30:00Z")

        assertEquals(LocalDate.of(2026, 7, 20), service.observeCurrentTimes(instant).first().date)
    }

    @Test
    fun `missing or invalid stored zone falls back compatibly`() {
        val fallback = ZoneId.of("Europe/Paris")

        assertEquals(fallback, null.toZoneIdOr(fallback))
        assertEquals(fallback, "not-a-zone".toZoneIdOr(fallback))
        assertEquals(ZoneId.of("Asia/Riyadh"), "Asia/Riyadh".toZoneIdOr(fallback))
    }

    private fun serviceWith(settings: PrayerSettings): CalculatedPrayerTimesService {
        val settingsService = mockk<PrayerSettingsService>()
        every { settingsService.settings } returns flowOf(settings)
        return CalculatedPrayerTimesService(settingsService)
    }
}
