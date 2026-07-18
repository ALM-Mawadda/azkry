package com.azkry.app.core.prayertimes

import com.azkry.app.core.models.Prayer
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimeCalculatorTest {
    private val mecca = GeoLocation(latitude = 21.4225, longitude = 39.8262)
    private val paris = GeoLocation(latitude = 48.8566, longitude = 2.3522)
    private val oslo = GeoLocation(latitude = 59.9139, longitude = 10.7522)

    @Test
    fun `equator equinox anchors sunrise near six and sunset near eighteen`() {
        val times = PrayerTimeCalculator.calculate(
            date = LocalDate.of(2026, 3, 20),
            location = GeoLocation(latitude = 0.0, longitude = 0.0),
            utcOffsetHours = 0.0,
            method = CalculationMethod.MuslimWorldLeague,
        )

        assertWithinMinutes(LocalTime.of(6, 0), times[Prayer.Sunrise], toleranceMinutes = 15)
        assertWithinMinutes(LocalTime.of(12, 0), times[Prayer.Dhuhr], toleranceMinutes = 15)
        assertWithinMinutes(LocalTime.of(18, 0), times[Prayer.Maghrib], toleranceMinutes = 15)
    }

    @Test
    fun `times are strictly ordered across seasons in mecca`() {
        listOf(
            LocalDate.of(2026, 1, 15),
            LocalDate.of(2026, 4, 15),
            LocalDate.of(2026, 7, 17),
            LocalDate.of(2026, 10, 15),
        ).forEach { date ->
            val times = PrayerTimeCalculator.calculate(
                date = date,
                location = mecca,
                utcOffsetHours = 3.0,
                method = CalculationMethod.UmmAlQura,
            )

            val ordered = listOf(
                Prayer.Fajr,
                Prayer.Sunrise,
                Prayer.Dhuhr,
                Prayer.Asr,
                Prayer.Maghrib,
                Prayer.Isha,
            ).map { times[it] }

            ordered.zipWithNext().forEach { (earlier, later) ->
                assertTrue("expected $earlier < $later on $date", earlier < later)
            }
        }
    }

    @Test
    fun `hanafi asr is later than shafii asr`() {
        val date = LocalDate.of(2026, 7, 17)
        val shafii = PrayerTimeCalculator.calculate(
            date = date,
            location = paris,
            utcOffsetHours = 2.0,
            method = CalculationMethod.France15,
            asrMadhab = AsrMadhab.Shafii,
        )
        val hanafi = PrayerTimeCalculator.calculate(
            date = date,
            location = paris,
            utcOffsetHours = 2.0,
            method = CalculationMethod.France15,
            asrMadhab = AsrMadhab.Hanafi,
        )

        assertTrue(hanafi[Prayer.Asr] > shafii[Prayer.Asr])
    }

    @Test
    fun `umm al qura isha is ninety minutes after maghrib`() {
        val times = PrayerTimeCalculator.calculate(
            date = LocalDate.of(2026, 2, 1),
            location = mecca,
            utcOffsetHours = 3.0,
            method = CalculationMethod.UmmAlQura,
        )

        val maghribMinutes = times[Prayer.Maghrib].toSecondOfDay() / 60
        val ishaMinutes = times[Prayer.Isha].toSecondOfDay() / 60
        assertTrue(abs(ishaMinutes - maghribMinutes - 90) <= 1)
    }

    @Test
    fun `smaller fajr angle yields later fajr`() {
        val date = LocalDate.of(2026, 7, 17)
        val fifteen = PrayerTimeCalculator.calculate(
            date = date,
            location = paris,
            utcOffsetHours = 2.0,
            method = CalculationMethod.France15,
        )
        val twelve = PrayerTimeCalculator.calculate(
            date = date,
            location = paris,
            utcOffsetHours = 2.0,
            method = CalculationMethod.France12,
        )

        assertTrue(twelve[Prayer.Fajr] > fifteen[Prayer.Fajr])
    }

    @Test
    fun `angle based rule produces usable times in high latitude summer`() {
        val times = PrayerTimeCalculator.calculate(
            date = LocalDate.of(2026, 6, 21),
            location = oslo,
            utcOffsetHours = 2.0,
            method = CalculationMethod.MuslimWorldLeague,
            highLatitudeRule = HighLatitudeRule.AngleBased,
        )

        // At Oslo's midsummer the sun sets but astronomical twilight never
        // occurs, so raw Fajr/Isha are undefined; the fallback must still
        // yield times with Fajr before Sunrise.
        assertTrue(times[Prayer.Fajr] < times[Prayer.Sunrise])
        Prayer.entries.forEach { prayer ->
            // Accessing every entry asserts nothing degenerated into NaN
            // (LocalTime conversion would have thrown on NaN input).
            times[prayer]
        }
    }

    private fun assertWithinMinutes(expected: LocalTime, actual: LocalTime, toleranceMinutes: Int) {
        val diff = abs(expected.toSecondOfDay() - actual.toSecondOfDay()) / 60
        assertTrue("expected $actual within $toleranceMinutes min of $expected", diff <= toleranceMinutes)
    }
}
