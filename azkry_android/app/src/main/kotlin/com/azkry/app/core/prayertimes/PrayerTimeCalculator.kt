package com.azkry.app.core.prayertimes

import com.azkry.app.core.models.Prayer
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
)

data class PrayerTimes(
    val times: Map<Prayer, LocalTime>,
) {
    operator fun get(prayer: Prayer): LocalTime = times.getValue(prayer)
}

/**
 * Astronomical prayer time calculation (solar position per the standard
 * approximate ephemeris used by prayer-time libraries; accuracy is within a
 * minute or two of reference implementations, which matches the tolerance the
 * app communicates to users in the settings help text).
 *
 * All computation is pure Kotlin so it runs in local unit tests.
 */
object PrayerTimeCalculator {
    private const val SUNRISE_SUNSET_ANGLE = 0.833

    fun calculate(
        date: LocalDate,
        location: GeoLocation,
        utcOffsetHours: Double,
        method: CalculationMethod,
        asrMadhab: AsrMadhab = AsrMadhab.Shafii,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.AngleBased,
    ): PrayerTimes {
        val jdBase = julianDay(date)

        // Times in floating hours of local solar time, refined once: the sun
        // position is re-evaluated at each estimate, which is the standard
        // fixed-point iteration and converges well within two passes.
        var fajr = 5.0
        var sunrise = 6.0
        var dhuhr = 12.0
        var asr = 13.0
        var maghrib = 18.0
        var isha = 18.0

        repeat(2) {
            fajr = solarTimeForAngle(jdBase, fajr, location, method.fajrAngle, before = true)
            sunrise = solarTimeForAngle(jdBase, sunrise, location, SUNRISE_SUNSET_ANGLE, before = true)
            dhuhr = midDay(jdBase, dhuhr)
            asr = asrTime(jdBase, asr, location, asrMadhab)
            maghrib = solarTimeForAngle(jdBase, maghrib, location, SUNRISE_SUNSET_ANGLE, before = false)
            isha = when {
                method.ishaAngle != null ->
                    solarTimeForAngle(jdBase, isha, location, method.ishaAngle, before = false)
                else -> maghrib + (method.ishaMinutesAfterMaghrib ?: 0) / 60.0
            }
        }

        if (highLatitudeRule != HighLatitudeRule.None) {
            val night = fixHour(sunrise - maghrib)
            fajr = adjustForHighLatitude(
                time = fajr,
                base = sunrise,
                angle = method.fajrAngle,
                night = night,
                rule = highLatitudeRule,
                before = true,
            )
            if (method.ishaAngle != null) {
                isha = adjustForHighLatitude(
                    time = isha,
                    base = maghrib,
                    angle = method.ishaAngle,
                    night = night,
                    rule = highLatitudeRule,
                    before = false,
                )
            }
        }

        val solarToLocal = utcOffsetHours - location.longitude / 15.0
        fun toLocalTime(solarHours: Double): LocalTime {
            require(!solarHours.isNaN()) {
                "Prayer time undefined for $location on $date; a HighLatitudeRule is required"
            }
            val local = fixHour(solarHours + solarToLocal)
            val totalMinutes = (local * 60.0 + 0.5).toInt() % (24 * 60)
            return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
        }

        return PrayerTimes(
            times = mapOf(
                Prayer.Fajr to toLocalTime(fajr),
                Prayer.Sunrise to toLocalTime(sunrise),
                Prayer.Dhuhr to toLocalTime(dhuhr),
                Prayer.Asr to toLocalTime(asr),
                Prayer.Maghrib to toLocalTime(maghrib),
                Prayer.Isha to toLocalTime(isha),
            ),
        )
    }

    private fun julianDay(date: LocalDate): Double {
        var year = date.year
        var month = date.monthValue
        if (month <= 2) {
            year -= 1
            month += 12
        }
        val a = floor(year / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (year + 4716)) +
            floor(30.6001 * (month + 1)) +
            date.dayOfMonth + b - 1524.5
    }

    /** Solar declination (degrees) and equation of time (hours) at a Julian date. */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        val declination = toDegrees(asin(sinDeg(e) * sinDeg(l)))
        val rightAscension = fixHour(toDegrees(atan2(cosDeg(e) * sinDeg(l), cosDeg(l))) / 15.0)
        val equationOfTime = q / 15.0 - rightAscension
        return declination to normalizeEquationOfTime(equationOfTime)
    }

    /** Keeps the equation of time in its natural ±½h band after the fixHour wrap. */
    private fun normalizeEquationOfTime(eqt: Double): Double = when {
        eqt > 12 -> eqt - 24
        eqt < -12 -> eqt + 24
        else -> eqt
    }

    private fun midDay(jdBase: Double, estimate: Double): Double {
        val (_, eqt) = sunPosition(jdBase + estimate / 24.0)
        return fixHour(12 - eqt)
    }

    /**
     * Local solar time at which the sun reaches [angle] degrees below the
     * horizon, before (morning) or after (evening) midday. Returns NaN inside
     * polar twilight where the sun never reaches the angle; high-latitude
     * adjustment then substitutes a fallback.
     */
    private fun solarTimeForAngle(
        jdBase: Double,
        estimate: Double,
        location: GeoLocation,
        angle: Double,
        before: Boolean,
    ): Double {
        val (declination, _) = sunPosition(jdBase + estimate / 24.0)
        val midday = midDay(jdBase, estimate)
        val cosHourAngle = (-sinDeg(angle) - sinDeg(declination) * sinDeg(location.latitude)) /
            (cosDeg(declination) * cosDeg(location.latitude))
        if (cosHourAngle < -1 || cosHourAngle > 1) return Double.NaN
        val hourAngle = toDegrees(acos(cosHourAngle)) / 15.0
        return if (before) midday - hourAngle else midday + hourAngle
    }

    private fun asrTime(
        jdBase: Double,
        estimate: Double,
        location: GeoLocation,
        madhab: AsrMadhab,
    ): Double {
        val (declination, _) = sunPosition(jdBase + estimate / 24.0)
        val midday = midDay(jdBase, estimate)
        val altitude = toDegrees(
            atan(1.0 / (madhab.shadowFactor + tanDeg(kotlin.math.abs(location.latitude - declination)))),
        )
        val cosHourAngle = (sinDeg(altitude) - sinDeg(declination) * sinDeg(location.latitude)) /
            (cosDeg(declination) * cosDeg(location.latitude))
        if (cosHourAngle < -1 || cosHourAngle > 1) return Double.NaN
        return midday + toDegrees(acos(cosHourAngle)) / 15.0
    }

    private fun adjustForHighLatitude(
        time: Double,
        base: Double,
        angle: Double,
        night: Double,
        rule: HighLatitudeRule,
        before: Boolean,
    ): Double {
        val portion = when (rule) {
            HighLatitudeRule.MiddleOfTheNight -> night / 2.0
            HighLatitudeRule.SeventhOfTheNight -> night / 7.0
            HighLatitudeRule.AngleBased -> angle / 60.0 * night
            HighLatitudeRule.None -> return time
        }
        val diff = if (before) fixHour(base - time) else fixHour(time - base)
        if (time.isNaN() || diff > portion) {
            return if (before) base - portion else base + portion
        }
        return time
    }

    private fun fixAngle(angle: Double): Double = angle.mod(360.0)

    private fun fixHour(hour: Double): Double = hour.mod(24.0)

    private fun toDegrees(radians: Double): Double = Math.toDegrees(radians)

    private fun sinDeg(degrees: Double): Double = sin(Math.toRadians(degrees))

    private fun cosDeg(degrees: Double): Double = cos(Math.toRadians(degrees))

    private fun tanDeg(degrees: Double): Double = tan(Math.toRadians(degrees))
}
