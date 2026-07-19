package com.azkry.app.features.mushaf.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.features.mushaf.models.JuzStart
import com.azkry.app.features.mushaf.models.Khatmah
import com.azkry.app.features.mushaf.models.LastRead
import com.azkry.app.features.mushaf.models.QuranBookmark
import com.azkry.app.features.mushaf.models.SurahInfo
import com.azkry.app.features.mushaf.services.QuranService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MushafTab { Surahs, Juzs, Pages }

@Immutable
data class PageEntry(
    val page: Int,
    val surahName: String,
)

@Immutable
data class KhatmahProgress(
    val dayNumber: Int,
    val totalDays: Int,
    val targetPage: Int,
    val currentPage: Int,
)

@Immutable
data class MushafUiState(
    val surahs: List<SurahInfo> = emptyList(),
    val juzs: List<JuzStart> = emptyList(),
    val pages: List<PageEntry> = emptyList(),
    val selectedTab: MushafTab = MushafTab.Surahs,
    val lastRead: LastRead? = null,
    val bookmarks: List<QuranBookmark> = emptyList(),
    val khatmah: KhatmahProgress? = null,
    val isLoading: Boolean = true,
) {
    fun surahInfo(number: Int): SurahInfo? = surahs.firstOrNull { it.number == number }

    /** The surah a mushaf page belongs to (the last surah starting at or before it). */
    fun surahForPage(page: Int): SurahInfo? = surahs.lastOrNull { it.page <= page }
}

@HiltViewModel
class MushafViewModel @Inject constructor(
    private val quranService: QuranService,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {
    private val index = MutableStateFlow(MushafUiState())

    val state: StateFlow<MushafUiState> =
        combine(
            index.asStateFlow(),
            quranService.lastRead,
            quranService.bookmarks,
            quranService.khatmah,
            currentDateProvider.observeCurrentDate(),
        ) { current, lastRead, bookmarks, khatmah, currentDate ->
            current.copy(
                lastRead = lastRead,
                bookmarks = bookmarks,
                khatmah = khatmah?.let { plan -> progressOf(plan, lastRead, currentDate) },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MushafUiState(),
        )

    init {
        viewModelScope.launch {
            val quranIndex = quranService.index()
            val surahs = quranIndex.surahs
            val pages = (1..Khatmah.TOTAL_PAGES).map { page ->
                PageEntry(
                    page = page,
                    surahName = surahs.lastOrNull { it.page <= page }?.name.orEmpty(),
                )
            }
            index.update {
                it.copy(
                    surahs = surahs,
                    juzs = quranIndex.juzs,
                    pages = pages,
                    isLoading = false,
                )
            }
        }
    }

    fun onTabSelected(tab: MushafTab) {
        index.update { it.copy(selectedTab = tab) }
    }

    fun onStartKhatmah(totalDays: Int) {
        viewModelScope.launch {
            quranService.startKhatmah(totalDays, currentDateProvider.currentDate().toDateKey())
        }
    }

    fun onFinishKhatmah() {
        viewModelScope.launch {
            quranService.finishKhatmah()
        }
    }

    private fun progressOf(
        plan: Khatmah,
        lastRead: LastRead?,
        currentDate: LocalDate,
    ): KhatmahProgress {
        val start = runCatching { LocalDate.parse(plan.startDateKey) }.getOrNull() ?: currentDate
        val dayNumber = (ChronoUnit.DAYS.between(start, currentDate) + 1)
            .coerceIn(1, plan.totalDays.toLong())
            .toInt()
        val targetPage = ceil(Khatmah.TOTAL_PAGES.toDouble() * dayNumber / plan.totalDays)
            .toInt()
            .coerceAtMost(Khatmah.TOTAL_PAGES)
        return KhatmahProgress(
            dayNumber = dayNumber,
            totalDays = plan.totalDays,
            targetPage = targetPage,
            currentPage = lastRead?.page ?: 1,
        )
    }
}
