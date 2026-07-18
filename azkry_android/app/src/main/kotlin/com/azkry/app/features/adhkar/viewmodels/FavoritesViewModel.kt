package com.azkry.app.features.adhkar.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.features.adhkar.services.AdhkarService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class FavoritesUiState(
    val items: List<DhikrCounterItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val adhkarService: AdhkarService,
) : ViewModel() {
    val state: StateFlow<FavoritesUiState> =
        combine(
            adhkarService.observeFavorites(),
            adhkarService.observeAllDailyCounts(todayKey()),
        ) { favorites, counts ->
            val countsById = counts.associate { it.dhikrId to it.count }
            FavoritesUiState(
                items = favorites.map { dhikr ->
                    DhikrCounterItem(
                        dhikr = dhikr,
                        count = countsById[dhikr.id] ?: 0,
                        isFavorite = true,
                    )
                },
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

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
            adhkarService.setFavorite(item.dhikr.id, false)
        }
    }

    private fun todayKey(): String = LocalDate.now().toDateKey()
}
