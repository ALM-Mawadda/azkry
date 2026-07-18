package com.azkry.app.features.notifications.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.azkry.app.MainActivity
import com.azkry.app.R
import com.azkry.app.core.models.labelRes
import com.azkry.app.core.notifications.AlarmScheduler
import com.azkry.app.core.notifications.NotificationChannels
import com.azkry.app.features.notifications.models.NotificationDestination
import com.azkry.app.features.notifications.models.PlannedReminder
import com.azkry.app.features.notifications.models.ReminderKind
import com.azkry.app.features.notifications.models.ReminderPlanner
import com.azkry.app.features.notifications.receivers.ReminderAlarmReceiver
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import com.azkry.app.features.widgets.AzkryWidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Owns the reminder chain: exactly one alarm is outstanding — the next
 * enabled reminder. When it fires (or settings, boot, or prayer-settings
 * changes re-plan), the chain continues from the current prayer times. The
 * same entry points keep the ongoing next-prayer notification and the
 * home-screen widgets fresh.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsService: NotificationSettingsService,
    private val prayerTimesService: PrayerTimesService,
    private val alarmScheduler: AlarmScheduler,
    private val widgetUpdater: AzkryWidgetUpdater,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)

    suspend fun rescheduleNext(now: LocalDateTime = LocalDateTime.now()) {
        val settings = settingsService.settings.first()
        updateOngoingNotification(settings, now)
        widgetUpdater.updateAll()

        if (settings.enabledKinds.isEmpty() && !settings.nextPrayerOngoing) {
            alarmScheduler.cancel(alarmPendingIntent(kindName = null, isPre = false))
            return
        }

        val today = now.toLocalDate()
        val timesByDate = listOf(today, today.plusDays(1)).associateWith { date ->
            prayerTimesService.observeTimes(date).first().times.times
        }

        // With only the ongoing notification enabled, the chain still follows
        // every adhan so the countdown rolls over on time.
        val effectiveKinds = settings.enabledKinds.ifEmpty { ReminderKind.adhanKinds.toSet() }
        val next = ReminderPlanner.nextReminder(
            now = now,
            enabled = effectiveKinds,
            preAdhanEnabled = settings.preAdhanEnabled,
        ) { date -> timesByDate[date].orEmpty() }

        if (next != null) {
            schedule(next)
        }
    }

    suspend fun onAlarmFired(kindName: String?, isPre: Boolean) {
        val settings = settingsService.settings.first()
        val kind = ReminderKind.fromName(kindName)
        if (kind != null && kind in settings.enabledKinds) {
            showNotification(kind, isPre)
        }
        rescheduleNext()
    }

    private fun schedule(reminder: PlannedReminder) {
        val triggerAtMillis = reminder.at
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        alarmScheduler.scheduleExact(
            triggerAtMillis,
            alarmPendingIntent(reminder.kind.name, reminder.isPreReminder),
        )
    }

    // Guarded by hasPostPermission(); the check lives in a helper lint
    // cannot trace across.
    @SuppressLint("MissingPermission")
    private suspend fun updateOngoingNotification(
        settings: NotificationSettings,
        now: LocalDateTime,
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!settings.nextPrayerOngoing || !hasPostPermission()) {
            manager.cancel(ONGOING_NOTIFICATION_ID)
            return
        }

        val dayTimes = prayerTimesService.observeTimes(now.toLocalDate()).first()
        val next = prayerTimesService.nextPrayer(now, dayTimes)
        val atMillis = next.at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val notification = NotificationCompat.Builder(context, NotificationChannels.NEXT_PRAYER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                context.getString(
                    R.string.notif_next_prayer_title,
                    context.getString(next.prayer.labelRes()),
                    next.at.toLocalTime().format(timeFormatter),
                ),
            )
            .setContentText(dayTimes.cityName)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(atMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setContentIntent(contentIntent(NotificationDestination.PrayerTimes))
            .build()

        manager.notify(ONGOING_NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    private suspend fun showNotification(kind: ReminderKind, isPre: Boolean) {
        if (!hasPostPermission()) return

        val (channel, title, text) = notificationContent(kind, isPre)
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(NotificationDestination.forKind(kind)))
            .build()

        NotificationManagerCompat.from(context).notify(kind.ordinal + if (isPre) 100 else 0, notification)
    }

    private suspend fun notificationContent(
        kind: ReminderKind,
        isPre: Boolean,
    ): Triple<String, String, String> = when (kind) {
        ReminderKind.MorningAdhkar -> Triple(
            NotificationChannels.ADHKAR_REMINDERS,
            context.getString(R.string.notif_morning_adhkar),
            context.getString(R.string.notif_morning_adhkar_text),
        )

        ReminderKind.EveningAdhkar -> Triple(
            NotificationChannels.ADHKAR_REMINDERS,
            context.getString(R.string.notif_evening_adhkar),
            context.getString(R.string.notif_evening_adhkar_text),
        )

        else -> {
            val prayer = requireNotNull(kind.prayer)
            val prayerName = context.getString(prayer.labelRes())
            val dayTimes = prayerTimesService.observeTimes(LocalDate.now()).first()
            if (isPre) {
                Triple(
                    NotificationChannels.ADHKAR_REMINDERS,
                    context.getString(
                        R.string.notif_pre_adhan_title,
                        prayerName,
                        ReminderPlanner.PRE_ADHAN_MINUTES,
                    ),
                    dayTimes.cityName,
                )
            } else {
                Triple(
                    NotificationChannels.ADHAN,
                    context.getString(R.string.notif_adhan_title, prayerName),
                    context.getString(
                        R.string.notif_adhan_text,
                        dayTimes.times[prayer].format(timeFormatter),
                        dayTimes.cityName,
                    ),
                )
            }
        }
    }

    private fun contentIntent(destination: NotificationDestination): PendingIntent =
        PendingIntent.getActivity(
            context,
            destination.ordinal,
            Intent(context, MainActivity::class.java).apply {
                putExtra(NotificationDestination.EXTRA, destination.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun hasPostPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun alarmPendingIntent(kindName: String?, isPre: Boolean): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            if (kindName != null) {
                putExtra(ReminderAlarmReceiver.EXTRA_KIND, kindName)
                putExtra(ReminderAlarmReceiver.EXTRA_PRE, isPre)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val REQUEST_CODE = 4001
        const val ONGOING_NOTIFICATION_ID = 900
    }
}
