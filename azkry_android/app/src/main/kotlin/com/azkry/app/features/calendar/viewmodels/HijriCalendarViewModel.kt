package com.azkry.app.features.calendar.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.app.AppSettingsService
import com.azkry.app.features.calendar.models.HijriMonth
import com.azkry.app.features.calendar.models.HijriMonthGrid
import java.time.LocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HijriCalendarViewModel @Inject constructor(
    private val appSettingsService: AppSettingsService,
) : ViewModel() {
    private val monthOffset = MutableStateFlow(0L)

    val month: StateFlow<HijriMonth?> =
        combine(monthOffset, appSettingsService.settings) { offset, appSettings ->
            HijriMonthGrid.monthAtOffset(
                monthOffset = offset,
                today = LocalDate.now().plusDays(appSettings.hijriOffsetDays.toLong()),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val hijriOffsetDays: StateFlow<Int> =
        appSettingsService.settings
            .map { it.hijriOffsetDays }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0,
            )

    fun onHijriOffsetSelected(offsetDays: Int) {
        viewModelScope.launch {
            appSettingsService.setHijriOffsetDays(offsetDays)
        }
    }

    fun onPreviousMonth() {
        monthOffset.update { it - 1 }
    }

    fun onNextMonth() {
        monthOffset.update { it + 1 }
    }

    fun onBackToCurrentMonth() {
        monthOffset.update { 0L }
    }
}
