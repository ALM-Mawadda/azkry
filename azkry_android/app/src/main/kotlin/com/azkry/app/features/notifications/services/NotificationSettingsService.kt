package com.azkry.app.features.notifications.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azkry.app.features.notifications.models.ReminderKind
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class NotificationSettings(
    /** Reminders start disabled; the user opts in per kind. */
    val enabledKinds: Set<ReminderKind> = emptySet(),
    /** Heads-up 15 minutes before every enabled adhan. */
    val preAdhanEnabled: Boolean = false,
    /** Persistent silent notification counting down to the next prayer. */
    val nextPrayerOngoing: Boolean = false,
)

interface NotificationSettingsService {
    val settings: Flow<NotificationSettings>

    suspend fun setKindEnabled(kind: ReminderKind, enabled: Boolean)
    suspend fun setPreAdhanEnabled(enabled: Boolean)
    suspend fun setNextPrayerOngoing(enabled: Boolean)
}

@Singleton
class DataStoreNotificationSettingsService @Inject constructor(
    @ApplicationContext context: Context,
) : NotificationSettingsService {
    private val dataStore = context.notificationSettingsDataStore

    override val settings: Flow<NotificationSettings> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                NotificationSettings(
                    enabledKinds = preferences[Keys.EnabledKinds]
                        .orEmpty()
                        .mapNotNull(ReminderKind::fromName)
                        .toSet(),
                    preAdhanEnabled = preferences[Keys.PreAdhan] ?: false,
                    nextPrayerOngoing = preferences[Keys.NextPrayerOngoing] ?: false,
                )
            }

    override suspend fun setKindEnabled(kind: ReminderKind, enabled: Boolean) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.EnabledKinds].orEmpty()
            preferences[Keys.EnabledKinds] = if (enabled) {
                current + kind.name
            } else {
                current - kind.name
            }
        }
    }

    override suspend fun setPreAdhanEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.PreAdhan] = enabled }
    }

    override suspend fun setNextPrayerOngoing(enabled: Boolean) {
        dataStore.edit { it[Keys.NextPrayerOngoing] = enabled }
    }

    private object Keys {
        val EnabledKinds = stringSetPreferencesKey("enabledKinds")
        val PreAdhan = booleanPreferencesKey("preAdhan")
        val NextPrayerOngoing = booleanPreferencesKey("nextPrayerOngoing")
    }
}

private val Context.notificationSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_settings",
)
