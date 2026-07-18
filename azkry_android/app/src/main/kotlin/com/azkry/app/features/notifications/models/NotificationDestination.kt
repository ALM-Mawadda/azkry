package com.azkry.app.features.notifications.models

import android.content.Intent

/** Where a tapped notification should land inside the app. */
enum class NotificationDestination {
    MorningAdhkar,
    EveningAdhkar,
    PrayerTimes,
    ;

    companion object {
        const val EXTRA = "notificationDestination"

        fun fromIntent(intent: Intent?): NotificationDestination? =
            intent?.getStringExtra(EXTRA)?.let { name ->
                entries.firstOrNull { it.name == name }
            }

        fun forKind(kind: ReminderKind): NotificationDestination = when (kind) {
            ReminderKind.MorningAdhkar -> MorningAdhkar
            ReminderKind.EveningAdhkar -> EveningAdhkar
            else -> PrayerTimes
        }
    }
}
