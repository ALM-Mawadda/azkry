package com.azkry.app.app

import android.content.Intent

/** Root-shell destination requested by an external entry point. */
enum class NotificationDestination(
    val intentValue: String,
    val requestCode: Int,
) {
    MorningAdhkar("MorningAdhkar", 0),
    EveningAdhkar("EveningAdhkar", 1),
    PrayerTimes("PrayerTimes", 2),
    ;

    companion object {
        const val EXTRA = "notificationDestination"

        fun fromIntent(intent: Intent?): NotificationDestination? =
            intent?.getStringExtra(EXTRA)?.let { value ->
                entries.firstOrNull { it.intentValue == value }
            }
    }
}
