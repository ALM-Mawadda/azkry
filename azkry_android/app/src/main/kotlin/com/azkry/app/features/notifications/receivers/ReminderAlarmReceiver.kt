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

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val kindName = intent.getStringExtra(EXTRA_KIND)
        val isPre = intent.getBooleanExtra(EXTRA_PRE, false)
        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                reminderScheduler.onAlarmFired(kindName, isPre)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_KIND = "reminderKind"
        const val EXTRA_PRE = "reminderPre"
    }
}
