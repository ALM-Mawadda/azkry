package com.azkry.app.features.adhkar.viewmodels

import app.cash.turbine.test
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.preview.Samples
import com.azkry.app.features.adhkar.services.AdhkarService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DhikrReaderViewModelTest {
    private lateinit var service: FakeAdhkarService
    private lateinit var viewModel: DhikrReaderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        service = FakeAdhkarService()
        viewModel = DhikrReaderViewModel(service)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads category items with zero counts`() = runTest {
        viewModel.start(Samples.morningCategory.id)

        viewModel.state.test {
            val loaded = awaitItemMatching { !it.isLoading }
            assertEquals(Samples.morningCategory, loaded.category)
            assertEquals(Samples.adhkar.size, loaded.items.size)
            assertTrue(loaded.items.all { it.count == 0 })
            assertFalse(loaded.allCompleted)
        }
    }

    @Test
    fun `tapping increments up to the repeat count`() = runTest {
        viewModel.start(Samples.morningCategory.id)

        viewModel.state.test {
            val loaded = awaitItemMatching { !it.isLoading }
            val ayah = loaded.items.first { it.dhikr.id == Samples.ayatAlKursi.id }

            viewModel.onDhikrTapped(ayah)
            val afterFirstTap = awaitItemMatching { state ->
                state.items.first { it.dhikr.id == ayah.dhikr.id }.count == 1
            }
            assertTrue(afterFirstTap.items.first { it.dhikr.id == ayah.dhikr.id }.isComplete)

            // Repeat count is 1: further taps must not exceed it.
            viewModel.onDhikrTapped(afterFirstTap.items.first { it.dhikr.id == ayah.dhikr.id })
            expectNoEvents()
        }
    }

    @Test
    fun `long pressing the counter resets it`() = runTest {
        viewModel.start(Samples.morningCategory.id)

        viewModel.state.test {
            val loaded = awaitItemMatching { !it.isLoading }
            val ayah = loaded.items.first { it.dhikr.id == Samples.ayatAlKursi.id }

            viewModel.onDhikrTapped(ayah)
            val tapped = awaitItemMatching { state ->
                state.items.first { it.dhikr.id == ayah.dhikr.id }.count == 1
            }

            viewModel.onCounterLongPressed(tapped.items.first { it.dhikr.id == ayah.dhikr.id })
            awaitItemMatching { state ->
                state.items.first { it.dhikr.id == ayah.dhikr.id }.count == 0
            }
        }
    }

    @Test
    fun `finish button completes every counter`() = runTest {
        viewModel.start(Samples.morningCategory.id)

        viewModel.state.test {
            awaitItemMatching { !it.isLoading }

            viewModel.onFinishedTapped()
            val completed = awaitItemMatching { it.allCompleted }
            assertEquals(completed.items.size, completed.completedCount)
        }
    }

    private suspend fun app.cash.turbine.TurbineTestContext<DhikrReaderUiState>.awaitItemMatching(
        predicate: (DhikrReaderUiState) -> Boolean,
    ): DhikrReaderUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}

private class FakeAdhkarService : AdhkarService {
    private val counts = MutableStateFlow<Map<Long, Int>>(emptyMap())

    override fun observeCategories(): Flow<List<DhikrCategory>> = flowOf(Samples.categories)

    override suspend fun categoryById(id: Long): DhikrCategory? =
        Samples.categories.firstOrNull { it.id == id }

    override fun observeAdhkar(categoryId: Long): Flow<List<Dhikr>> =
        flowOf(Samples.adhkar.filter { it.categoryId == categoryId })

    override fun observeDailyCounts(categoryId: Long, date: String): Flow<List<DhikrDailyCount>> =
        counts.map { byId ->
            byId.map { (dhikrId, count) ->
                DhikrDailyCount(date = date, dhikrId = dhikrId, count = count)
            }
        }

    override suspend fun incrementCount(dhikr: Dhikr, date: String) {
        val current = counts.value[dhikr.id] ?: 0
        if (current >= dhikr.repeatCount) return
        counts.value = counts.value + (dhikr.id to current + 1)
    }

    override suspend fun resetCount(dhikrId: Long, date: String) {
        counts.value = counts.value - dhikrId
    }

    override suspend fun completeAll(adhkar: List<Dhikr>, date: String) {
        counts.value = counts.value + adhkar.associate { it.id to it.repeatCount }
    }

    override fun observeFavorites(): Flow<List<Dhikr>> =
        favorites.map { ids -> Samples.adhkar.filter { it.id in ids } }

    override fun observeFavoriteIds(): Flow<Set<Long>> = favorites

    override fun observeAllDailyCounts(date: String): Flow<List<DhikrDailyCount>> =
        counts.map { byId ->
            byId.map { (dhikrId, count) ->
                DhikrDailyCount(date = date, dhikrId = dhikrId, count = count)
            }
        }

    override suspend fun setFavorite(dhikrId: Long, favorite: Boolean) {
        favorites.value = if (favorite) favorites.value + dhikrId else favorites.value - dhikrId
    }

    override suspend fun categoryByKey(key: String): DhikrCategory? =
        Samples.categories.firstOrNull { it.key == key }

    override suspend fun search(query: String): List<Dhikr> =
        Samples.adhkar.filter { it.text.contains(query) }

    private val favorites = MutableStateFlow<Set<Long>>(emptySet())
}
