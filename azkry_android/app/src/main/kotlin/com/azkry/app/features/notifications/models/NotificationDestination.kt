package com.azkry.app.features.notifications.models

import com.azkry.app.app.NotificationDestination

/** Maps notification-domain kinds onto root-shell navigation destinations. */
fun ReminderKind.notificationDestination(): NotificationDestination = when (this) {
    ReminderKind.MorningAdhkar -> NotificationDestination.MorningAdhkar
    ReminderKind.EveningAdhkar -> NotificationDestination.EveningAdhkar
    else -> NotificationDestination.PrayerTimes
}
