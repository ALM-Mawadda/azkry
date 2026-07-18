package com.azkry.app.features.search.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.utilities.containsArabic
import com.azkry.app.features.adhkar.services.AdhkarService
import com.azkry.app.features.mushaf.models.SurahInfo
import com.azkry.app.features.mushaf.services.QuranService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@Immutable
data class DhikrSearchHit(
    val dhikr: Dhikr,
    val categoryTitle: String,
)

@Immutable
data class SearchUiState(
    val query: String = "",
    val adhkarHits: List<DhikrSearchHit> = emptyList(),
    val surahHits: List<SurahInfo> = emptyList(),
) {
    val isEmptyResult: Boolean
        get() = query.isNotBlank() && adhkarHits.isEmpty() && surahHits.isEmpty()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val adhkarService: AdhkarService,
    private val quranService: QuranService,
) : ViewModel() {
    private val query = MutableStateFlow("")

    @OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val results = query
        .debounce(250)
        .mapLatest { text ->
            if (text.isBlank()) return@mapLatest Pair(emptyList<DhikrSearchHit>(), emptyList<SurahInfo>())
            val categories = categoriesById()
            val adhkarHits = adhkarService.search(text).map { dhikr ->
                DhikrSearchHit(
                    dhikr = dhikr,
                    categoryTitle = categories[dhikr.categoryId]?.title.orEmpty(),
                )
            }
            val surahHits = quranService.index().surahs.filter { surah ->
                surah.name.containsArabic(text) || surah.englishName.contains(text, ignoreCase = true)
            }
            Pair(adhkarHits, surahHits)
        }

    val state: StateFlow<SearchUiState> =
        combine(query.asStateFlow(), results) { text, (adhkarHits, surahHits) ->
            SearchUiState(query = text, adhkarHits = adhkarHits, surahHits = surahHits)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState(),
        )

    fun onQueryChanged(text: String) {
        query.value = text
    }

    private var cachedCategories: Map<Long, DhikrCategory>? = null

    private suspend fun categoriesById(): Map<Long, DhikrCategory> =
        cachedCategories ?: adhkarService.observeCategories().first()
            .associateBy(DhikrCategory::id)
            .also { cachedCategories = it }
}
