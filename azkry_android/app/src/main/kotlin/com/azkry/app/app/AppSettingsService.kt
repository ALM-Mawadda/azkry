package com.azkry.app.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class AppSettings(
    // The app ships Arabic-only, so Arabic (and RTL) is the default even on
    // non-Arabic devices; AppLanguage.Default becomes meaningful once more
    // locales exist.
    val language: AppLanguage = AppLanguage.Arabic,
    val appearance: Appearance = Appearance.Dark,
    /** Moon-sighting correction applied to every hijri display, in days. */
    val hijriOffsetDays: Int = 0,
)

interface AppSettingsService {
    val settings: Flow<AppSettings>

    suspend fun setLanguage(language: AppLanguage)
    suspend fun setAppearance(appearance: Appearance)
    suspend fun setHijriOffsetDays(offsetDays: Int)
}

@Singleton
class DataStoreAppSettingsService @Inject constructor(
    @ApplicationContext context: Context,
) : AppSettingsService {
    private val dataStore = context.appSettingsDataStore

    override val settings: Flow<AppSettings> =
        dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences ->
                val stored = AppLanguage.fromCode(preferences[AppStateKeys.AppLanguage])
                AppSettings(
                    language = if (stored == AppLanguage.Default) AppLanguage.Arabic else stored,
                    appearance = Appearance.fromName(preferences[AppStateKeys.Appearance]),
                    hijriOffsetDays = preferences[AppStateKeys.HijriOffsetDays]
                        ?.toIntOrNull()
                        ?.coerceIn(-2, 2)
                        ?: 0,
                )
            }

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            val code = language.code
            if (code == null) {
                preferences.remove(AppStateKeys.AppLanguage)
            } else {
                preferences[AppStateKeys.AppLanguage] = code
            }
        }
    }

    override suspend fun setAppearance(appearance: Appearance) {
        dataStore.edit { preferences ->
            preferences[AppStateKeys.Appearance] = appearance.name
        }
    }

    override suspend fun setHijriOffsetDays(offsetDays: Int) {
        dataStore.edit { preferences ->
            preferences[AppStateKeys.HijriOffsetDays] = offsetDays.coerceIn(-2, 2).toString()
        }
    }
}

object EmptyAppSettingsService : AppSettingsService {
    override val settings: Flow<AppSettings> = flowOf(AppSettings())

    override suspend fun setLanguage(language: AppLanguage) = Unit
    override suspend fun setAppearance(appearance: Appearance) = Unit
    override suspend fun setHijriOffsetDays(offsetDays: Int) = Unit
}

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
)
