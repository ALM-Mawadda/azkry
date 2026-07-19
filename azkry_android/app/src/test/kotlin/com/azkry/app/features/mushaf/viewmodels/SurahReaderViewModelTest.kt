package com.azkry.app.features.mushaf.viewmodels

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.azkry.app.features.mushaf.models.Khatmah
import com.azkry.app.features.mushaf.models.LastRead
import com.azkry.app.features.mushaf.models.QuranAyah
import com.azkry.app.features.mushaf.models.QuranBookmark
import com.azkry.app.features.mushaf.models.QuranIndex
import com.azkry.app.features.mushaf.models.SurahContent
import com.azkry.app.features.mushaf.services.QuranService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SurahReaderViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `same surah request honors a new start ayah`() = runTest {
        val service = FakeQuranService()
        val viewModel = SurahReaderViewModel(service)

        viewModel.state.test {
            awaitItem()

            viewModel.start(surahNumber = 1, startAyah = 3)
            val first = awaitLoadedState()
            assertEquals(1, first.initialPageIndex)
            assertEquals(101, first.currentPage)

            viewModel.start(surahNumber = 1, startAyah = 1)
            val replacement = awaitLoadedState()
            assertEquals(0, replacement.initialPageIndex)
            assertEquals(100, replacement.currentPage)
        }

        assertEquals(2, service.surahRequests.count { it == 1 })
        assertEquals(1, service.lastReads.last().ayahNumber)
    }

    @Test
    fun `same surah request honors a new start page`() = runTest {
        val service = FakeQuranService()
        val viewModel = SurahReaderViewModel(service)

        viewModel.state.test {
            awaitItem()

            viewModel.start(surahNumber = 1, startPage = 102)
            val first = awaitLoadedState()
            assertEquals(2, first.initialPageIndex)
            assertEquals(102, first.currentPage)

            viewModel.start(surahNumber = 1, startPage = 101)
            val replacement = awaitLoadedState()
            assertEquals(1, replacement.initialPageIndex)
            assertEquals(101, replacement.currentPage)
        }

        assertEquals(2, service.surahRequests.count { it == 1 })
        assertEquals(101, service.lastReads.last().page)
    }

    @Test
    fun `overlapping load cancels the old request and only publishes the replacement`() = runTest {
        val service = FakeQuranService(heldSurah = 1, suppressHeldCancellation = true)
        val viewModel = SurahReaderViewModel(service)

        viewModel.state.test {
            awaitItem()

            viewModel.start(surahNumber = 1)
            service.heldRequestStarted.await()

            viewModel.start(surahNumber = 2)
            service.heldRequestCancelled.await()
            val replacement = awaitLoadedState()
            assertEquals(2, replacement.surah?.number)

            service.releaseHeldRequest.complete(Unit)
            yield()

            assertEquals(2, viewModel.state.value.surah?.number)
            assertTrue(service.lastReads.all { it.surahNumber == 2 })
        }
    }

    private suspend fun TurbineTestContext<SurahReaderUiState>.awaitLoadedState(): SurahReaderUiState {
        while (true) {
            val item = awaitItem()
            if (!item.isLoading) return item
        }
    }
}

private class FakeQuranService(
    private val heldSurah: Int? = null,
    private val suppressHeldCancellation: Boolean = false,
) : QuranService {
    val surahRequests = mutableListOf<Int>()
    val lastReads = mutableListOf<LastRead>()
    val heldRequestStarted = CompletableDeferred<Unit>()
    val heldRequestCancelled = CompletableDeferred<Unit>()
    val releaseHeldRequest = CompletableDeferred<Unit>()

    override suspend fun index(): QuranIndex = QuranIndex(emptyList(), emptyList())

    override suspend fun surah(number: Int): SurahContent {
        surahRequests += number
        if (number == heldSurah) {
            heldRequestStarted.complete(Unit)
            try {
                releaseHeldRequest.await()
            } catch (error: CancellationException) {
                heldRequestCancelled.complete(Unit)
                if (!suppressHeldCancellation) throw error
                withContext(NonCancellable) { releaseHeldRequest.await() }
            }
        }
        return contents.getValue(number)
    }

    override val lastRead: Flow<LastRead?> = MutableStateFlow(null)

    override suspend fun saveLastRead(lastRead: LastRead) {
        lastReads += lastRead
    }

    override val bookmarks: Flow<List<QuranBookmark>> = MutableStateFlow(emptyList())

    override suspend fun toggleBookmark(bookmark: QuranBookmark) = Unit

    override val khatmah: Flow<Khatmah?> = MutableStateFlow(null)

    override suspend fun startKhatmah(totalDays: Int, startDateKey: String) = Unit

    override suspend fun finishKhatmah() = Unit

    private companion object {
        val contents = mapOf(
            1 to SurahContent(
                number = 1,
                name = "First",
                ayahs = listOf(
                    QuranAyah(numberInSurah = 1, text = "one", page = 100, juz = 1),
                    QuranAyah(numberInSurah = 2, text = "two", page = 100, juz = 1),
                    QuranAyah(numberInSurah = 3, text = "three", page = 101, juz = 1),
                    QuranAyah(numberInSurah = 4, text = "four", page = 102, juz = 1),
                ),
            ),
            2 to SurahContent(
                number = 2,
                name = "Second",
                ayahs = listOf(
                    QuranAyah(numberInSurah = 1, text = "one", page = 200, juz = 2),
                ),
            ),
        )
    }
}
