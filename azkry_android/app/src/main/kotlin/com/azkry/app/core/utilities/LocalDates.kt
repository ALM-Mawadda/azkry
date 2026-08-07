package com.azkry.app.core.utilities

import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private const val DATE_CHECK_INTERVAL_MILLIS = 60_000L

/**
 * Single testable source for local daily keys. The cold flow emits immediately
 * when collection resumes and at midnight while it remains active.
 */
@Singleton
class CurrentDateProvider private constructor(
    private val datesOverride: StateFlow<LocalDate>?,
) {
    @Inject
    constructor() : this(datesOverride = null)

    fun currentDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
        datesOverride?.value ?: LocalDate.now(zoneId)

    fun observeCurrentDate(zoneId: ZoneId = ZoneId.systemDefault()): Flow<LocalDate> =
        datesOverride ?: systemDates(zoneId)

    private fun systemDates(zoneId: ZoneId): Flow<LocalDate> = flow {
        while (currentCoroutineContext().isActive) {
            val now = ZonedDateTime.now(zoneId)
            val today = now.toLocalDate()
            emit(today)

            val nextMidnight = today.plusDays(1).atStartOfDay(zoneId)
            val untilMidnightMillis = Duration.between(now, nextMidnight)
                .toMillis()
                .coerceAtLeast(1L)
            delay(untilMidnightMillis.coerceAtMost(DATE_CHECK_INTERVAL_MILLIS))
        }
    }.distinctUntilChanged()

    companion object {
        internal fun forTest(dates: StateFlow<LocalDate>): CurrentDateProvider =
            CurrentDateProvider(datesOverride = dates)
    }
}

/** Canonical date key used across the local database: local ISO yyyy-MM-dd. */
fun LocalDate.toDateKey(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

private val hijriMonthFormatter =
    DateTimeFormatter.ofPattern("MMMM", Locale.forLanguageTag("ar"))

/** Hijri day of month, e.g. 4. */
fun LocalDate.toHijriDay(): Int =
    HijrahDate.from(atStartOfDay()).get(ChronoField.DAY_OF_MONTH)

/** Hijri month name in Arabic, e.g. "صفر". */
fun LocalDate.toHijriMonthName(): String =
    HijrahDate.from(atStartOfDay()).format(hijriMonthFormatter)

/** e.g. "1448-02-03" for the tracking header. */
fun LocalDate.toHijriIso(): String {
    val hijrah = HijrahDate.from(atStartOfDay())
    val year = hijrah.get(ChronoField.YEAR)
    val month = hijrah.get(ChronoField.MONTH_OF_YEAR)
    val day = hijrah.get(ChronoField.DAY_OF_MONTH)
    return "%04d-%02d-%02d".format(Locale.ENGLISH, year, month, day)
}
