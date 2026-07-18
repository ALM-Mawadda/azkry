package com.azkry.app.features.notifications.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.features.notifications.models.ReminderKind
import com.azkry.app.features.notifications.services.NotificationSettings
import com.azkry.app.features.notifications.services.NotificationSettingsService
import com.azkry.app.features.notifications.services.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val settingsService: NotificationSettingsService,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val settings: StateFlow<NotificationSettings?> =
        settingsService.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun onKindToggled(kind: ReminderKind, enabled: Boolean) {
        viewModelScope.launch {
            settingsService.setKindEnabled(kind, enabled)
            reminderScheduler.rescheduleNext()
        }
    }

    fun onPreAdhanToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsService.setPreAdhanEnabled(enabled)
            reminderScheduler.rescheduleNext()
        }
    }

    fun onNextPrayerOngoingToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsService.setNextPrayerOngoing(enabled)
            reminderScheduler.rescheduleNext()
        }
    }
}
