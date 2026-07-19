package com.azkry.app.features.tracking.viewmodels

import app.cash.turbine.test
import com.azkry.app.app.AppSettings
import com.azkry.app.app.AppSettingsService
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.prayertimes.PrayerTimes
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.features.prayertimes.services.DayPrayerTimes
import com.azkry.app.features.prayertimes.services.PrayerSettings
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import com.azkry.app.features.tracking.services.DayTracking
import com.azkry.app.features.tracking.services.WorshipTrackingService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rollover rebuilds the week and writes the new date`() = runTest {
        val dates = MutableStateFlow(LocalDate.of(2026, 1, 2))
        val trackingService = mockk<WorshipTrackingService>()
        val prayerTimesService = mockk<PrayerTimesService>()
        val appSettingsService = mockk<AppSettingsService>()
        val emptyDay = DayTracking(
            completedPrayers = emptySet(),
            completedTasks = emptySet(),
            adhkarProgress = emptyList(),
        )
        every { trackingService.observeDay(any()) } returns flowOf(emptyDay)
        every { trackingService.observeWeekPercents(any()) } answers {
            flowOf(firstArg<List<String>>().associateWith { 0 })
        }
        every { prayerTimesService.observeTimes(any()) } answers {
            flowOf(dayPrayerTimes(firstArg()))
        }
        every { appSettingsService.settings } returns flowOf(AppSettings())
        coEvery { trackingService.setPrayerCompleted(any(), any(), any()) } just Runs

        val viewModel = TrackingViewModel(
            trackingService = trackingService,
            prayerTimesService = prayerTimesService,
            appSettingsService = appSettingsService,
            currentDateProvider = CurrentDateProvider.forTest(dates),
        )

        viewModel.state.test {
            val friday = awaitLoaded { it.gregorianDate == "2026-01-02" }
            assertEquals(27, friday.weekDays.first().dayOfMonth)

            dates.value = LocalDate.of(2026, 1, 3)
            val saturday = awaitLoaded { it.gregorianDate == "2026-01-03" }
            assertEquals(3, saturday.weekDays.first().dayOfMonth)
            viewModel.onPrayerToggled(saturday.prayers.first())

            coVerify(exactly = 1) {
                trackingService.setPrayerCompleted("2026-01-03", Prayer.Fajr, true)
            }
            verify { trackingService.observeDay("2026-01-02") }
            verify { trackingService.observeDay("2026-01-03") }
            verify {
                trackingService.observeWeekPercents(
                    match { it.first() == "2026-01-03" && it.last() == "2026-01-09" },
                )
            }
        }
    }

    private fun dayPrayerTimes(date: LocalDate): DayPrayerTimes = DayPrayerTimes(
        date = date,
        times = PrayerTimes(
            mapOf(
                Prayer.Fajr to LocalTime.of(5, 0),
                Prayer.Sunrise to LocalTime.of(6, 30),
                Prayer.Dhuhr to LocalTime.of(12, 0),
                Prayer.Asr to LocalTime.of(15, 0),
                Prayer.Maghrib to LocalTime.of(18, 0),
                Prayer.Isha to LocalTime.of(20, 0),
            ),
        ),
        settings = PrayerSettings(),
    )

    private suspend fun app.cash.turbine.TurbineTestContext<TrackingUiState?>.awaitLoaded(
        predicate: (TrackingUiState) -> Boolean,
    ): TrackingUiState {
        while (true) {
            val item = awaitItem()
            if (item != null && predicate(item)) return item
        }
    }
}
