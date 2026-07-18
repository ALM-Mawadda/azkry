package com.azkry.app.core.utilities

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

/** Canonical date key used across the local database: local ISO yyyy-MM-dd. */
fun LocalDate.toDateKey(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

private val hijriDayMonthFormatter =
    DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ar"))

private val hijriMonthFormatter =
    DateTimeFormatter.ofPattern("MMMM", Locale.forLanguageTag("ar"))

/** e.g. "٣ صفر" rendered with Arabic month names: "3 صفر". */
fun LocalDate.toHijriDayMonth(): String =
    HijrahDate.from(atStartOfDay()).format(hijriDayMonthFormatter)

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
