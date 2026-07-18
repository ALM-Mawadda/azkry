package com.azkry.app.features.calendar.models

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

data class HijriDayCell(
    val hijriDay: Int,
    val gregorianDay: Int,
    val gregorianDate: LocalDate,
    val isToday: Boolean,
)

data class HijriMonth(
    /** e.g. "صفر 1448". */
    val title: String,
    /** Blank leading cells so day 1 lands on its weekday (Saturday-first row). */
    val leadingBlanks: Int,
    val days: List<HijriDayCell>,
)

/**
 * Pure month-grid computation over `java.time`'s Umm al-Qura Hijrah
 * chronology, keyed by an offset in months from the current hijri month.
 */
object HijriMonthGrid {
    private val titleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("ar"))

    fun monthAtOffset(monthOffset: Long, today: LocalDate = LocalDate.now()): HijriMonth {
        val todayHijrah = HijrahDate.from(today)
        val target = todayHijrah.plus(monthOffset, ChronoUnit.MONTHS)
        val firstOfMonth = target.with(ChronoField.DAY_OF_MONTH, 1)
        val monthLength = target.range(ChronoField.DAY_OF_MONTH).maximum.toInt()

        val firstGregorian = LocalDate.from(firstOfMonth)
        // Saturday-first grid, matching the tracking week strip.
        val leadingBlanks = ((firstGregorian.dayOfWeek.value - DayOfWeek.SATURDAY.value) + 7) % 7

        val days = (0 until monthLength).map { dayIndex ->
            val gregorian = firstGregorian.plusDays(dayIndex.toLong())
            HijriDayCell(
                hijriDay = dayIndex + 1,
                gregorianDay = gregorian.dayOfMonth,
                gregorianDate = gregorian,
                isToday = gregorian == today,
            )
        }

        return HijriMonth(
            title = firstOfMonth.format(titleFormatter),
            leadingBlanks = leadingBlanks,
            days = days,
        )
    }
}
