package com.azkry.app.features.adhkar.viewmodels

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

@HiltViewModel
class ExclusiveViewModel @Inject constructor(
    adhkarService: AdhkarService,
) : ViewModel() {
    val categories: StateFlow<List<DhikrCategory>> =
        adhkarService.observeCategories()
            .map { categories -> categories.filter { it.isExclusive } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )
}
