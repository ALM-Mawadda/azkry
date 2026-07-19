package com.azkry.app.features.adhkar.viewmodels

import app.cash.turbine.test
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.features.adhkar.services.AdhkarService
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
class FavoritesViewModelTest {
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
        val firstDate = LocalDate.of(2026, 1, 1)
        val secondDate = firstDate.plusDays(1)
        val dates = MutableStateFlow(firstDate)
        val dhikr = Samples.ayatAlKursi
        val service = mockk<AdhkarService>()
        every { service.observeFavorites() } returns flowOf(listOf(dhikr))
        every { service.observeAllDailyCounts("2026-01-01") } returns flowOf(
            listOf(DhikrDailyCount(date = "2026-01-01", dhikrId = dhikr.id, count = 1)),
        )
        every { service.observeAllDailyCounts("2026-01-02") } returns flowOf(emptyList())
        coEvery { service.incrementCount(dhikr, any()) } just Runs

        val viewModel = FavoritesViewModel(
            adhkarService = service,
            currentDateProvider = CurrentDateProvider.forTest(dates),
        )

        viewModel.state.test {
            val firstDay = awaitLoaded { it.items.singleOrNull()?.count == 1 }
            assertEquals(1, firstDay.items.single().count)

            dates.value = secondDate
            val secondDay = awaitLoaded { it.items.singleOrNull()?.count == 0 }
            viewModel.onDhikrTapped(secondDay.items.single())

            coVerify(exactly = 1) { service.incrementCount(dhikr, "2026-01-02") }
            verify(exactly = 1) { service.observeAllDailyCounts("2026-01-01") }
            verify(exactly = 1) { service.observeAllDailyCounts("2026-01-02") }
        }
    }

    private suspend fun app.cash.turbine.TurbineTestContext<FavoritesUiState>.awaitLoaded(
        predicate: (FavoritesUiState) -> Boolean,
    ): FavoritesUiState {
        while (true) {
            val item = awaitItem()
            if (!item.isLoading && predicate(item)) return item
        }
    }
}
