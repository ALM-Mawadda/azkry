package com.azkry.app.features.prayertimes.services

import com.azkry.app.core.models.Prayer
import com.azkry.app.core.prayertimes.PrayerTimeCalculator
import com.azkry.app.core.prayertimes.PrayerTimes
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class NextPrayer(
    val prayer: Prayer,
    val at: ZonedDateTime,
    val remaining: Duration,
)

data class DayPrayerTimes(
    val date: LocalDate,
    val times: PrayerTimes,
    val settings: PrayerSettings,
) {
    val cityName: String get() = settings.cityName
}

interface PrayerTimesService {
    /** Times for [date] at the configured location, re-emitted on settings change. */
    fun observeTimes(date: LocalDate): Flow<DayPrayerTimes>

    /** Times for the civil date containing [instant] in the configured location's zone. */
    fun observeCurrentTimes(instant: Instant): Flow<DayPrayerTimes>

    /**
     * The next upcoming obligatory adhan relative to [now] given that day's
     * [times] — rolls to the next day's Fajr after Isha.
     */
    fun nextPrayer(now: Instant, times: DayPrayerTimes): NextPrayer
}

@Singleton
class CalculatedPrayerTimesService @Inject constructor(
    private val settingsService: PrayerSettingsService,
) : PrayerTimesService {
    override fun observeTimes(date: LocalDate): Flow<DayPrayerTimes> =
        settingsService.settings.map { settings ->
            dayTimes(date, settings)
        }

    override fun observeCurrentTimes(instant: Instant): Flow<DayPrayerTimes> =
        settingsService.settings.map { settings ->
            dayTimes(instant.atZone(settings.zoneId).toLocalDate(), settings)
        }

    override fun nextPrayer(now: Instant, times: DayPrayerTimes): NextPrayer {
        val zoneId = times.settings.zoneId
        val localNow = LocalDateTime.ofInstant(now, zoneId)
        val today = times.date
        val upcoming = Prayer.obligatory
            .map { prayer -> prayer to LocalDateTime.of(today, times.times[prayer]) }
            .firstOrNull { (_, at) -> !at.isBefore(localNow) }

        if (upcoming != null) {
            val (prayer, localAt) = upcoming
            val at = localAt.atZone(zoneId)
            return NextPrayer(
                prayer = prayer,
                at = at,
                remaining = Duration.between(now, at.toInstant()),
            )
        }

        // Past Isha: calculate the next day independently because Fajr shifts
        // from one date to the next (and its UTC offset may cross a DST change).
        val tomorrow = today.plusDays(1)
        val tomorrowTimes = calculate(tomorrow, times.settings)
        val fajrTomorrow = LocalDateTime.of(tomorrow, tomorrowTimes[Prayer.Fajr]).atZone(zoneId)
        return NextPrayer(
            prayer = Prayer.Fajr,
            at = fajrTomorrow,
            remaining = Duration.between(now, fajrTomorrow.toInstant()),
        )
    }

    private fun dayTimes(date: LocalDate, settings: PrayerSettings): DayPrayerTimes =
        DayPrayerTimes(
            date = date,
            times = calculate(date, settings),
            settings = settings,
        )

    private fun calculate(date: LocalDate, settings: PrayerSettings): PrayerTimes {
        // Noon represents the date's civil offset after typical overnight DST
        // transitions, while keeping the calculator's one-offset-per-day model.
        val utcOffsetHours = date.atTime(LocalTime.NOON)
            .atZone(settings.zoneId)
            .offset
            .totalSeconds / 3600.0
        return PrayerTimeCalculator.calculate(
            date = date,
            location = settings.location,
            utcOffsetHours = utcOffsetHours,
            method = settings.method,
            asrMadhab = settings.asrMadhab,
            highLatitudeRule = settings.highLatitudeRule,
        )
    }
}
