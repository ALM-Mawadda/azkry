package com.azkry.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import com.azkry.app.R

object NotificationChannels {
    // v2: channel sound is immutable after creation, so the adhan channel id
    // was bumped when the bundled adhan recording replaced the default tone.
    const val ADHAN = "adhan_v2"
    const val ADHKAR_REMINDERS = "adhkar_reminders"
    const val NEXT_PRAYER = "next_prayer"

    private const val LEGACY_ADHAN = "adhan"

    fun create(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.deleteNotificationChannel(LEGACY_ADHAN)

        val adhanSound = Uri.parse("android.resource://${context.packageName}/${R.raw.adhan}")
        val adhanChannel = NotificationChannel(
            ADHAN,
            context.getString(R.string.notification_channel_adhan),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            setSound(
                adhanSound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            enableVibration(true)
        }

        notificationManager.createNotificationChannels(
            listOf(
                adhanChannel,
                NotificationChannel(
                    ADHKAR_REMINDERS,
                    context.getString(R.string.notification_channel_adhkar),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
                NotificationChannel(
                    NEXT_PRAYER,
                    context.getString(R.string.notification_channel_next_prayer),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            ),
        )
    }
}
