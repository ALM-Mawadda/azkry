package com.azkry.app.features.prayertimes.viewmodels

import com.azkry.app.app.AppSettings
import com.azkry.app.app.AppSettingsService
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.prayertimes.CalculationMethod
import com.azkry.app.core.prayertimes.GeoLocation
import com.azkry.app.core.prayertimes.PrayerTimeCalculator
import com.azkry.app.core.prayertimes.PrayerTimes
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.features.prayertimes.models.PrayerDayView
import com.azkry.app.features.prayertimes.services.CalculatedPrayerTimesService
import com.azkry.app.features.prayertimes.services.PrayerSettings
import com.azkry.app.features.prayertimes.services.PrayerSettingsService
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerDayViewModelTest {
    private val date = LocalDate.of(2026, 7, 19)

    private val mecca = PrayerSettings(
        cityName = "مكة المكرمة",
        location = GeoLocation(latitude = 21.4225, longitude = 39.8262),
        zoneId = ZoneId.of("Asia/Riyadh"),
        method = CalculationMethod.UmmAlQura,
    )

    private val paris = PrayerSettings(
        cityName = "باريس",
        location = GeoLocation(latitude = 48.8566, longitude = 2.3522),
        zoneId = ZoneId.of("Europe/Paris"),
        method = CalculationMethod.France15,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `displayed times are calculated for the configured location`() = runTest {
        for (settings in listOf(mecca, paris)) {
            val state = stateAt(noonOn(date, settings), settings)
            val expected = calculate(date, settings)

            assertEquals(settings.cityName, state.cityName)
            for (prayer in Prayer.entries) {
                assertEquals(
                    "${settings.cityName} ${prayer.name}",
                    expected[prayer].format(timeFormatter),
                    state.rows.single { it.prayer == prayer }.time,
                )
            }
        }
    }

    @Test
    fun `a different location yields different times for the same day`() = runTest {
        val inMecca = stateAt(noonOn(date, mecca), mecca)
        val inParis = stateAt(noonOn(date, paris), paris)

        assertNotEquals(
            inMecca.rows.map { it.time },
            inParis.rows.map { it.time },
        )
    }

    @Test
    fun `past isha counts down to tomorrow's fajr without highlighting today's`() = runTest {
        val isha = calculate(date, mecca)[Prayer.Isha]
        val state = stateAt(instantOn(date, isha.plusMinutes(30), mecca), mecca)

        // The countdown keeps running past Isha...
        assertEquals(Prayer.Fajr, state.nextPrayer)
        assertTrue(state.countdown.isNotEmpty())
        // ...but it belongs to tomorrow, so no row on this day is "next".
        assertTrue(state.rows.none { it.isNext })
    }

    @Test
    fun `the upcoming prayer of the day is the highlighted row`() = runTest {
        val dhuhr = calculate(date, mecca)[Prayer.Dhuhr]
        val state = stateAt(instantOn(date, dhuhr.minusMinutes(30), mecca), mecca)

        assertEquals(Prayer.Dhuhr, state.nextPrayer)
        assertEquals(listOf(Prayer.Dhuhr), state.rows.filter { it.isNext }.map { it.prayer })
    }

    @Test
    fun `only sunrise carries a second line, and it is the calculated ishraq`() = runTest {
        val state = stateAt(noonOn(date, mecca), mecca)
        val expected = PrayerDayView.ishraqTime(calculate(date, mecca)[Prayer.Sunrise])

        assertEquals(
            expected.format(timeFormatter),
            state.rows.single { it.prayer == Prayer.Sunrise }.ishraqTime,
        )
        assertTrue(
            state.rows.filter { it.prayer != Prayer.Sunrise }.all { it.ishraqTime == null },
        )
    }

    @Test
    fun `stepping to another day drops the countdown and the forbidden window`() = runTest {
        val viewModel = viewModel(noonOn(date, mecca), mecca)
        viewModel.onNextDay()

        val state = viewModel.state.filterNotNull().first { !it.isToday }

        assertFalse(state.isToday)
        assertNull(state.nextPrayer)
        assertNull(state.forbiddenWindow)
        assertEquals("", state.countdown)
        assertTrue(state.rows.none { it.isNext })
        assertEquals(date.plusDays(1).toString(), state.dateKey)
    }

    @Test
    fun `returning to today restores the live day`() = runTest {
        val viewModel = viewModel(noonOn(date, mecca), mecca)
        viewModel.onPreviousDay()
        viewModel.state.filterNotNull().first { !it.isToday }

        viewModel.onBackToToday()

        val state = viewModel.state.filterNotNull().first { it.isToday }
        assertEquals(date.toString(), state.dateKey)
        assertEquals(Prayer.Dhuhr, state.nextPrayer)
    }

    private suspend fun stateAt(now: Instant, settings: PrayerSettings): PrayerDayUiState =
        viewModel(now, settings).state.filterNotNull().first()

    private fun viewModel(now: Instant, prayerSettings: PrayerSettings): PrayerDayViewModel {
        val settingsService = mockk<PrayerSettingsService> {
            every { settings } returns flowOf(prayerSettings)
        }
        val appSettingsService = mockk<AppSettingsService> {
            every { settings } returns flowOf(AppSettings())
        }
        return PrayerDayViewModel(
            prayerTimesService = CalculatedPrayerTimesService(settingsService),
            prayerSettingsService = settingsService,
            appSettingsService = appSettingsService,
            currentDateProvider = CurrentDateProvider.forTest(MutableStateFlow(date)),
            clock = Clock.fixed(now, prayerSettings.zoneId),
        )
    }

    private fun calculate(date: LocalDate, settings: PrayerSettings): PrayerTimes {
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

    private fun instantOn(date: LocalDate, time: LocalTime, settings: PrayerSettings): Instant =
        date.atTime(time).atZone(settings.zoneId).toInstant()

    private fun noonOn(date: LocalDate, settings: PrayerSettings): Instant =
        instantOn(date, LocalTime.NOON, settings)

    private companion object {
        val timeFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
    }
}
