package com.azkry.app.features.friday.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.features.friday.models.FridaySunnah
import com.azkry.app.features.friday.services.FridayService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class FridayUiState(
    val checks: Set<FridaySunnah> = emptySet(),
)

@HiltViewModel
class FridayViewModel @Inject constructor(
    private val fridayService: FridayService,
) : ViewModel() {
    val state: StateFlow<FridayUiState?> =
        fridayService.observeChecks(todayKey())
            .map(::FridayUiState)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    fun onSunnahToggled(sunnah: FridaySunnah, currentlyChecked: Boolean) {
        viewModelScope.launch {
            fridayService.setChecked(todayKey(), sunnah, !currentlyChecked)
        }
    }

    private fun todayKey(): String = LocalDate.now().toDateKey()
}
