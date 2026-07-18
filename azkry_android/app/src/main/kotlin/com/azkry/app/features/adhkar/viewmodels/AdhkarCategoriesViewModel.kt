package com.azkry.app.features.adhkar.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.isExclusive
import com.azkry.app.features.adhkar.services.AdhkarService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class AdhkarCategoriesUiState(
    val categories: List<DhikrCategory> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class AdhkarCategoriesViewModel @Inject constructor(
    adhkarService: AdhkarService,
) : ViewModel() {
    val state: StateFlow<AdhkarCategoriesUiState> =
        adhkarService.observeCategories()
            .map { categories ->
                AdhkarCategoriesUiState(
                    // Exclusive-section categories live behind their own screen.
                    categories = categories.filterNot { it.isExclusive },
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AdhkarCategoriesUiState(),
            )
}
