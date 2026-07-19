package com.azkry.app.features.adhkar.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.features.adhkar.services.AdhkarService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class DhikrCounterItem(
    val dhikr: Dhikr,
    val count: Int,
    val isFavorite: Boolean = false,
) {
    val isComplete: Boolean get() = count >= dhikr.repeatCount
    val remaining: Int get() = (dhikr.repeatCount - count).coerceAtLeast(0)
}

@Immutable
data class DhikrReaderUiState(
    val category: DhikrCategory? = null,
    val items: List<DhikrCounterItem> = emptyList(),
    val isLoading: Boolean = true,
) {
    val completedCount: Int get() = items.count { it.isComplete }
    val allCompleted: Boolean get() = items.isNotEmpty() && items.all { it.isComplete }
}

@HiltViewModel
class DhikrReaderViewModel @Inject constructor(
    private val adhkarService: AdhkarService,
    private val currentDateProvider: CurrentDateProvider,
) : ViewModel() {
    private val categoryId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DhikrReaderUiState> =
        combine(categoryId, currentDateProvider.observeCurrentDate()) { id, date -> id to date }
            .flatMapLatest { (id, date) ->
                if (id == null) return@flatMapLatest flowOf(DhikrReaderUiState())
                val dateKey = date.toDateKey()
                combine(
                    adhkarService.observeAdhkar(id),
                    adhkarService.observeDailyCounts(id, dateKey),
                    adhkarService.observeFavoriteIds(),
                    categoryFlow(id),
                ) { adhkar, counts, favoriteIds, category ->
                    val countsById = counts.associate { it.dhikrId to it.count }
                    DhikrReaderUiState(
                        category = category,
                        items = adhkar.map { dhikr ->
                            DhikrCounterItem(
                                dhikr = dhikr,
                                count = countsById[dhikr.id] ?: 0,
                                isFavorite = dhikr.id in favoriteIds,
                            )
                        },
                        isLoading = false,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DhikrReaderUiState(),
            )

    fun start(id: Long) {
        categoryId.value = id
    }

    fun onDhikrTapped(item: DhikrCounterItem) {
        viewModelScope.launch {
            adhkarService.incrementCount(item.dhikr, todayKey())
        }
    }

    fun onCounterLongPressed(item: DhikrCounterItem) {
        viewModelScope.launch {
            adhkarService.resetCount(item.dhikr.id, todayKey())
        }
    }

    fun onFavoriteToggled(item: DhikrCounterItem) {
        viewModelScope.launch {
            adhkarService.setFavorite(item.dhikr.id, !item.isFavorite)
        }
    }

    fun onFinishedTapped() {
        val items = state.value.items
        if (items.isEmpty()) return
        viewModelScope.launch {
            adhkarService.completeAll(items.map { it.dhikr }, todayKey())
        }
    }

    private fun categoryFlow(id: Long) =
        kotlinx.coroutines.flow.flow { emit(adhkarService.categoryById(id)) }

    private fun todayKey(): String = currentDateProvider.currentDate().toDateKey()
}
