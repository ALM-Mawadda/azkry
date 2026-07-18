package com.azkry.app.features.notifications.models

import androidx.annotation.StringRes
import com.azkry.app.R
import com.azkry.app.core.models.Prayer

/**
 * Everything the app can notify about. Adhan kinds fire at the prayer time;
 * the adhkar reminders are anchored to Fajr and Asr with a fixed offset.
 */
enum class ReminderKind(val prayer: Prayer?) {
    FajrAdhan(Prayer.Fajr),
    DhuhrAdhan(Prayer.Dhuhr),
    AsrAdhan(Prayer.Asr),
    MaghribAdhan(Prayer.Maghrib),
    IshaAdhan(Prayer.Isha),
    MorningAdhkar(null),
    EveningAdhkar(null),
    ;

    val isAdhan: Boolean get() = prayer != null

    companion object {
        val adhanKinds: List<ReminderKind> = entries.filter { it.isAdhan }

        fun fromName(name: String?): ReminderKind? =
            entries.firstOrNull { it.name == name }
    }
}

@StringRes
fun ReminderKind.titleRes(): Int = when (this) {
    ReminderKind.FajrAdhan -> R.string.prayer_fajr
    ReminderKind.DhuhrAdhan -> R.string.prayer_dhuhr
    ReminderKind.AsrAdhan -> R.string.prayer_asr
    ReminderKind.MaghribAdhan -> R.string.prayer_maghrib
    ReminderKind.IshaAdhan -> R.string.prayer_isha
    ReminderKind.MorningAdhkar -> R.string.notif_morning_adhkar
    ReminderKind.EveningAdhkar -> R.string.notif_evening_adhkar
}
