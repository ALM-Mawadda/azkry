package com.azkry.app.features.prayertimes.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.core.prayertimes.AsrMadhab
import com.azkry.app.core.prayertimes.CalculationMethod
import com.azkry.app.core.prayertimes.GeoLocation
import com.azkry.app.core.prayertimes.HighLatitudeRule
import com.azkry.app.features.notifications.services.ReminderScheduler
import com.azkry.app.features.prayertimes.services.LocationService
import com.azkry.app.features.prayertimes.services.PrayerSettings
import com.azkry.app.features.prayertimes.services.PrayerSettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PrayerTimesSettingsViewModel @Inject constructor(
    private val settingsService: PrayerSettingsService,
    private val locationService: LocationService,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val settings: StateFlow<PrayerSettings?> =
        settingsService.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val internalIsLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = internalIsLocating.asStateFlow()

    val hasLocationPermission: Boolean
        get() = locationService.hasPermission

    fun onMethodSelected(method: CalculationMethod) {
        applySetting { settingsService.setMethod(method) }
    }

    fun onAsrMadhabSelected(asrMadhab: AsrMadhab) {
        applySetting { settingsService.setAsrMadhab(asrMadhab) }
    }

    fun onHighLatitudeRuleSelected(rule: HighLatitudeRule) {
        applySetting { settingsService.setHighLatitudeRule(rule) }
    }

    fun onManualLocationEntered(cityName: String, latitude: Double, longitude: Double) {
        applySetting {
            settingsService.setAutoLocate(false)
            settingsService.setLocation(
                cityName = cityName,
                location = GeoLocation(latitude = latitude, longitude = longitude),
            )
        }
    }

    /** Requires the location permission to be granted already. */
    fun onUseCurrentLocation() {
        viewModelScope.launch {
            internalIsLocating.value = true
            try {
                val resolved = locationService.currentLocation() ?: return@launch
                settingsService.setAutoLocate(true)
                settingsService.setLocation(
                    cityName = resolved.cityName
                        ?: settings.value?.cityName
                        ?: PrayerSettings.DEFAULT_CITY_NAME,
                    location = resolved.location,
                )
                reminderScheduler.rescheduleNext()
            } finally {
                internalIsLocating.value = false
            }
        }
    }

    private fun applySetting(update: suspend () -> Unit) {
        viewModelScope.launch {
            update()
            // Prayer times moved, so the outstanding adhan alarm may be stale.
            reminderScheduler.rescheduleNext()
        }
    }
}
