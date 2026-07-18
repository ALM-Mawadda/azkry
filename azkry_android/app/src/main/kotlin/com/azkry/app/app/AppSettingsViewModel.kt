package com.azkry.app.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val settingsService: AppSettingsService,
) : ViewModel() {
    val settings: StateFlow<AppSettings?> =
        settingsService.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun onAppearanceSelected(appearance: Appearance) {
        viewModelScope.launch {
            settingsService.setAppearance(appearance)
        }
    }

    fun onHijriOffsetSelected(offsetDays: Int) {
        viewModelScope.launch {
            settingsService.setHijriOffsetDays(offsetDays)
        }
    }
}
