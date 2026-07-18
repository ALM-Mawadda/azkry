package com.azkry.app.features.qibla.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.features.prayertimes.services.PrayerSettingsService
import com.azkry.app.features.qibla.models.QiblaMath
import com.azkry.app.features.qibla.services.CompassService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@Immutable
data class QiblaUiState(
    val cityName: String,
    val bearingDegrees: Double,
    val azimuthDegrees: Double,
    val hasCompass: Boolean,
) {
    val needleRotationDegrees: Double
        get() = QiblaMath.needleRotation(bearingDegrees, azimuthDegrees)
}

@HiltViewModel
class QiblaViewModel @Inject constructor(
    settingsService: PrayerSettingsService,
    compassService: CompassService,
) : ViewModel() {
    val state: StateFlow<QiblaUiState?> =
        combine(
            settingsService.settings,
            compassService.observeAzimuth().onStart { emit(0.0) },
        ) { settings, azimuth ->
            QiblaUiState(
                cityName = settings.cityName,
                bearingDegrees = QiblaMath.bearingToKaaba(
                    latitude = settings.location.latitude,
                    longitude = settings.location.longitude,
                ),
                azimuthDegrees = azimuth,
                hasCompass = compassService.hasCompass,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
