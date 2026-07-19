package com.azkry.app.features.friday.viewmodels

import app.cash.turbine.test
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.features.friday.models.FridaySunnah
import com.azkry.app.features.friday.services.FridayService
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
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
class FridayViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rollover observes and writes the new date`() = runTest {
        val dates = MutableStateFlow(LocalDate.of(2026, 1, 1))
        val service = mockk<FridayService>()
        every { service.observeChecks("2026-01-01") } returns flowOf(setOf(FridaySunnah.Ghusl))
        every { service.observeChecks("2026-01-02") } returns flowOf(emptySet())
        coEvery { service.setChecked(any(), any(), any()) } just Runs

        val viewModel = FridayViewModel(
            fridayService = service,
            currentDateProvider = CurrentDateProvider.forTest(dates),
        )

        viewModel.state.test {
            val firstDay = awaitLoaded { FridaySunnah.Ghusl in it.checks }
            assertEquals(setOf(FridaySunnah.Ghusl), firstDay.checks)

            dates.value = LocalDate.of(2026, 1, 2)
            awaitLoaded { it.checks.isEmpty() }
            viewModel.onSunnahToggled(FridaySunnah.Perfume, currentlyChecked = false)

            coVerify(exactly = 1) {
                service.setChecked("2026-01-02", FridaySunnah.Perfume, true)
            }
            verify(exactly = 1) { service.observeChecks("2026-01-01") }
            verify(exactly = 1) { service.observeChecks("2026-01-02") }
        }
    }

    private suspend fun app.cash.turbine.TurbineTestContext<FridayUiState?>.awaitLoaded(
        predicate: (FridayUiState) -> Boolean,
    ): FridayUiState {
        while (true) {
            val item = awaitItem()
            if (item != null && predicate(item)) return item
        }
    }
}
