package com.azkry.app.features.notifications.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper over [AlarmManager] exact scheduling. Adhan needs to-the-minute
 * precision, so exact alarms are used whenever the platform allows; on API
 * 31–32 without the exact-alarm grant it degrades to a 10-minute window.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    fun scheduleExact(triggerAtMillis: Long, operation: PendingIntent) {
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        if (canBeExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation,
            )
        } else {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                10 * 60_000L,
                operation,
            )
        }
    }

    fun cancel(operation: PendingIntent) {
        alarmManager.cancel(operation)
    }
}
