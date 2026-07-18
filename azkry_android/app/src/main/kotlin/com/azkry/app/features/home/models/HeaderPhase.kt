package com.azkry.app.features.home.models

import com.azkry.app.core.models.DhikrCategoryKeys
import com.azkry.app.core.models.Prayer
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

/**
 * The sky the home header paints, derived from where "now" falls between the
 * day's prayer events — dawn glow at Fajr, blue sky with a sun at midday,
 * golden afternoon, sunset at Maghrib, stars at night.
 */
enum class HeaderPhase {
    Dawn,
    Day,
    Afternoon,
    Dusk,
    Night,
}

object HomeDayView {
    fun phaseFor(now: LocalTime, times: Map<Prayer, LocalTime>): HeaderPhase {
        val fajr = times[Prayer.Fajr] ?: return HeaderPhase.Night
        val sunrise = times[Prayer.Sunrise] ?: return HeaderPhase.Night
        val asr = times[Prayer.Asr] ?: return HeaderPhase.Night
        val maghrib = times[Prayer.Maghrib] ?: return HeaderPhase.Night
        val isha = times[Prayer.Isha] ?: return HeaderPhase.Night

        return when {
            now < fajr -> HeaderPhase.Night
            now < sunrise -> HeaderPhase.Dawn
            now < asr -> HeaderPhase.Day
            now < maghrib -> HeaderPhase.Afternoon
            now < isha -> HeaderPhase.Dusk
            else -> HeaderPhase.Night
        }
    }

    /**
     * The two events framing "now" for the header strip — the design shows
     * the event that just passed on the reading-start side and the upcoming
     * one opposite (e.g. الشروق 06:07 … الظهر 01:45 at midday). Before Fajr
     * the frame is yesterday's Isha and today's Fajr.
     */
    fun stripEvents(
        now: LocalTime,
        times: Map<Prayer, LocalTime>,
    ): Pair<Prayer, Prayer> {
        val ordered = Prayer.entries.filter { times.containsKey(it) }
        if (ordered.size < 2) return Prayer.Maghrib to Prayer.Isha

        val nextIndex = ordered.indexOfFirst { prayer -> now < times.getValue(prayer) }
        return when (nextIndex) {
            -1 -> ordered.last() to ordered.first() // past Isha: Isha → Fajr
            0 -> ordered.last() to ordered.first() // before Fajr: Isha → Fajr
            else -> ordered[nextIndex - 1] to ordered[nextIndex]
        }
    }

    /** The adhkar set the moment calls for (the nested home shortcut). */
    fun suggestedAdhkarKey(phase: HeaderPhase): String = when (phase) {
        HeaderPhase.Dawn, HeaderPhase.Day -> DhikrCategoryKeys.MORNING
        HeaderPhase.Afternoon, HeaderPhase.Dusk -> DhikrCategoryKeys.EVENING
        HeaderPhase.Night -> DhikrCategoryKeys.SLEEP
    }

    /**
     * Coarse Arabic phrase for the countdown row ("٤ دقائق", "ساعة و٥٦ دقيقة").
     * Minutes are rounded up so 3:08 left reads "بعد 4 دقائق" like the
     * reference; western digits match the app's clock style.
     */
    fun countdownPhrase(remaining: Duration): String {
        val totalSeconds = remaining.seconds.coerceAtLeast(0)
        if (totalSeconds < 60) return "أقل من دقيقة"
        val totalMinutes = (totalSeconds + 59) / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val hoursPart = when {
            hours == 0L -> null
            hours == 1L -> "ساعة"
            hours == 2L -> "ساعتين"
            hours in 3..10 -> "$hours ساعات"
            else -> "$hours ساعة"
        }
        val minutesPart = when {
            minutes == 0L -> null
            minutes == 1L -> "دقيقة"
            minutes == 2L -> "دقيقتين"
            minutes in 3..10 -> "$minutes دقائق"
            else -> "$minutes دقيقة"
        }
        return listOfNotNull(hoursPart, minutesPart).joinToString(separator = " و")
    }

    /** Precise ticking clock: H:MM:SS above an hour, M:SS below it. */
    fun countdownClock(remaining: Duration): String {
        val totalSeconds = remaining.seconds.coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
        }
    }
}
