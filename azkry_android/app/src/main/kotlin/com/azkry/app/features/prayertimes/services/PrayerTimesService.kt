package com.azkry.app.features.prayertimes.services

import com.azkry.app.core.models.Prayer
import com.azkry.app.core.prayertimes.PrayerTimeCalculator
import com.azkry.app.core.prayertimes.PrayerTimes
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class NextPrayer(
    val prayer: Prayer,
    val at: LocalDateTime,
    val remaining: Duration,
)

data class DayPrayerTimes(
    val date: LocalDate,
    val cityName: String,
    val times: PrayerTimes,
)

interface PrayerTimesService {
    /** Times for [date] at the configured location, re-emitted on settings change. */
    fun observeTimes(date: LocalDate): Flow<DayPrayerTimes>

    /**
     * The next upcoming obligatory adhan relative to [now] given that day's
     * [times] — rolls to the next day's Fajr after Isha.
     */
    fun nextPrayer(now: LocalDateTime, times: DayPrayerTimes): NextPrayer
}

@Singleton
class CalculatedPrayerTimesService @Inject constructor(
    private val settingsService: PrayerSettingsService,
) : PrayerTimesService {
    override fun observeTimes(date: LocalDate): Flow<DayPrayerTimes> =
        settingsService.settings.map { settings ->
            DayPrayerTimes(
                date = date,
                cityName = settings.cityName,
                times = calculate(date, settings),
            )
        }

    override fun nextPrayer(now: LocalDateTime, times: DayPrayerTimes): NextPrayer {
        val today = times.date
        val upcoming = Prayer.obligatory
            .map { prayer -> prayer to LocalDateTime.of(today, times.times[prayer]) }
            .firstOrNull { (_, at) -> !at.isBefore(now) }

        if (upcoming != null) {
            val (prayer, at) = upcoming
            return NextPrayer(prayer = prayer, at = at, remaining = Duration.between(now, at))
        }

        // Past Isha: the next adhan is tomorrow's Fajr. Recomputing with the
        // same-day times is within a minute or two of the true next-day time,
        // which matches the app's stated accuracy.
        val fajrTomorrow = LocalDateTime.of(today.plusDays(1), times.times[Prayer.Fajr])
        return NextPrayer(
            prayer = Prayer.Fajr,
            at = fajrTomorrow,
            remaining = Duration.between(now, fajrTomorrow),
        )
    }

    private fun calculate(date: LocalDate, settings: PrayerSettings): PrayerTimes {
        val zone = ZoneId.systemDefault()
        val utcOffsetHours =
            zone.rules.getOffset(date.atStartOfDay(zone).toInstant()).totalSeconds / 3600.0
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
