package com.azkry.app.features.prayertimes.models

import com.azkry.app.core.models.Prayer
import java.time.LocalTime

/** The three times at which voluntary prayer is discouraged. */
enum class ForbiddenWindow {
    /** After Fajr until the sun has risen a spear's length. */
    AfterFajr,

    /** While the sun stands at its zenith, until it declines. */
    Zawal,

    /** After Asr until the Maghrib adhan. */
    AfterAsr,
}

/**
 * Derivations layered on the calculated times for the user's configured
 * location. Everything here is a function of those computed times — the screen
 * never displays a value the app cannot derive.
 *
 * Iqama times deliberately have no place here: they are set by each mosque and
 * cannot be calculated, so the screen shows adhan times only.
 */
object PrayerDayView {
    /**
     * "قيد رمح" — the sun must clear the horizon by roughly this much before
     * prayer resumes. This single constant fixes both the end of the after-Fajr
     * forbidden window and the start of Ishraq, because they are the same
     * moment.
     */
    private const val SUNRISE_CLEARANCE_MINUTES = 15L

    /** The zenith window opens this long before the calculated solar noon. */
    private const val ZAWAL_LEAD_MINUTES = 10L

    /**
     * When Ishraq becomes permissible, derived from the calculated [sunrise].
     * This is the same instant the after-Fajr forbidden window closes.
     */
    fun ishraqTime(sunrise: LocalTime): LocalTime =
        sunrise.plusMinutes(SUNRISE_CLEARANCE_MINUTES)

    /** The forbidden window [now] falls in, or null when prayer is allowed. */
    fun forbiddenWindow(
        now: LocalTime,
        times: Map<Prayer, LocalTime>,
    ): ForbiddenWindow? {
        val fajr = times[Prayer.Fajr] ?: return null
        val sunrise = times[Prayer.Sunrise] ?: return null
        val dhuhr = times[Prayer.Dhuhr] ?: return null
        val asr = times[Prayer.Asr] ?: return null
        val maghrib = times[Prayer.Maghrib] ?: return null

        return when {
            now >= fajr && now < ishraqTime(sunrise) -> ForbiddenWindow.AfterFajr

            now >= dhuhr.minusMinutes(ZAWAL_LEAD_MINUTES) && now < dhuhr ->
                ForbiddenWindow.Zawal

            now >= asr && now < maghrib -> ForbiddenWindow.AfterAsr

            else -> null
        }
    }
}
