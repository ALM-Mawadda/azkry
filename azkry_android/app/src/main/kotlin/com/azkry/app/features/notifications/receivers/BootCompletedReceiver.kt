package com.azkry.app.features.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.azkry.app.core.coroutines.ApplicationScope
import com.azkry.app.features.notifications.services.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Alarms are lost on reboot and timezone change; re-plan the chain. */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIMEZONE_CHANGED &&
            action != Intent.ACTION_TIME_CHANGED
        ) {
            return
        }
        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                reminderScheduler.rescheduleNext()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
