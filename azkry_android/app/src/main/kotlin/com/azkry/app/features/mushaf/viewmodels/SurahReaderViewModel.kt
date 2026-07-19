package com.azkry.app.features.mushaf.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.features.mushaf.models.LastRead
import com.azkry.app.features.mushaf.models.QuranAyah
import com.azkry.app.features.mushaf.models.QuranBookmark
import com.azkry.app.features.mushaf.models.SurahContent
import com.azkry.app.features.mushaf.services.QuranService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Ayahs of one mushaf page, the reader's list unit. */
@Immutable
data class ReaderPage(
    val page: Int,
    val juz: Int,
    val ayahs: List<QuranAyah>,
)

@Immutable
data class SurahReaderUiState(
    val surah: SurahContent? = null,
    val pages: List<ReaderPage> = emptyList(),
    /** Index into [pages] the reader should start at (juz jumps, resume). */
    val initialPageIndex: Int = 0,
    val bookmarkedPages: Set<Int> = emptySet(),
    val currentPage: Int = 0,
    val isLoading: Boolean = true,
) {
    val isCurrentPageBookmarked: Boolean get() = currentPage in bookmarkedPages
}

@HiltViewModel
class SurahReaderViewModel @Inject constructor(
    private val quranService: QuranService,
) : ViewModel() {
    private val internalState = MutableStateFlow(SurahReaderUiState())

    val state: StateFlow<SurahReaderUiState> =
        combine(internalState.asStateFlow(), quranService.bookmarks) { current, bookmarks ->
            current.copy(bookmarkedPages = bookmarks.map { it.page }.toSet())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SurahReaderUiState(),
        )

    private var activeRequest: ReaderRequest? = null
    private var loadJob: Job? = null

    fun start(surahNumber: Int, startAyah: Int? = null, startPage: Int? = null) {
        val request = ReaderRequest(surahNumber, startAyah, startPage)
        if (activeRequest == request) return

        activeRequest = request
        loadJob?.cancel()
        internalState.value = SurahReaderUiState()
        loadJob = viewModelScope.launch {
            val content = quranService.surah(surahNumber)
            val pages = content.ayahs
                .groupBy { it.page }
                .map { (page, ayahs) -> ReaderPage(page = page, juz = ayahs.first().juz, ayahs = ayahs) }
                .sortedBy { it.page }
            val initialIndex = when {
                startPage != null ->
                    pages.indexOfFirst { it.page >= startPage }.coerceAtLeast(0)

                startAyah != null ->
                    pages.indexOfFirst { page -> page.ayahs.any { it.numberInSurah >= startAyah } }
                        .coerceAtLeast(0)

                else -> 0
            }
            currentCoroutineContext().ensureActive()
            if (activeRequest != request) return@launch
            internalState.update {
                SurahReaderUiState(
                    surah = content,
                    pages = pages,
                    initialPageIndex = initialIndex,
                    currentPage = pages.getOrNull(initialIndex)?.page ?: 0,
                    isLoading = false,
                )
            }
            saveLastRead(content, pages.getOrNull(initialIndex)?.ayahs?.firstOrNull())
        }
    }

    /** Called as the reader settles on a page so resume stays accurate. */
    fun onPageViewed(page: ReaderPage) {
        val content = internalState.value.surah ?: return
        internalState.update { it.copy(currentPage = page.page) }
        viewModelScope.launch {
            saveLastRead(content, page.ayahs.first())
        }
    }

    fun onBookmarkToggled() {
        val current = internalState.value
        val content = current.surah ?: return
        if (current.currentPage <= 0) return
        viewModelScope.launch {
            quranService.toggleBookmark(
                QuranBookmark(
                    surahNumber = content.number,
                    surahName = content.name,
                    page = current.currentPage,
                ),
            )
        }
    }

    private suspend fun saveLastRead(content: SurahContent, ayah: QuranAyah?) {
        quranService.saveLastRead(
            LastRead(
                surahNumber = content.number,
                surahName = content.name,
                ayahNumber = ayah?.numberInSurah ?: 1,
                page = ayah?.page ?: content.ayahs.first().page,
                timestampMillis = Instant.now().toEpochMilli(),
            ),
        )
    }

    private data class ReaderRequest(
        val surahNumber: Int,
        val startAyah: Int?,
        val startPage: Int?,
    )
}
