package com.azkry.app.features.main

import androidx.lifecycle.ViewModel
import com.azkry.app.app.NotificationDestination
import com.azkry.app.core.models.DhikrCategoryKeys
import com.azkry.app.features.adhkar.services.AdhkarService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainShellViewModel @Inject constructor(
    private val adhkarService: AdhkarService,
) : ViewModel() {
    /** Resolves a notification destination to the seeded category to open. */
    suspend fun categoryIdFor(destination: NotificationDestination): Long? {
        val key = when (destination) {
            NotificationDestination.MorningAdhkar -> DhikrCategoryKeys.MORNING
            NotificationDestination.EveningAdhkar -> DhikrCategoryKeys.EVENING
            NotificationDestination.PrayerTimes -> return null
        }
        return adhkarService.categoryByKey(key)?.id
    }
}
