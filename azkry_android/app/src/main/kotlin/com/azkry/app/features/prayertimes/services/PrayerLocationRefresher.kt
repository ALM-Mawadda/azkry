package com.azkry.app.features.prayertimes.services

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Silently refreshes the prayer location on app open when the user has opted
 * into auto-location and the permission is still granted. Failures are
 * ignored — the previous location stays valid.
 */
@Singleton
class PrayerLocationRefresher @Inject constructor(
    private val settingsService: PrayerSettingsService,
    private val locationService: LocationService,
) {
    suspend fun refreshIfAutoLocating() {
        val settings = settingsService.settings.first()
        if (!settings.autoLocate || !locationService.hasPermission) return
        val resolved = locationService.currentLocation() ?: return
        settingsService.setLocation(
            cityName = resolved.cityName ?: settings.cityName,
            location = resolved.location,
        )
    }
}
