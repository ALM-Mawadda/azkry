package com.azkry.app.features.prayertimes.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azkry.app.core.prayertimes.AsrMadhab
import com.azkry.app.core.prayertimes.CalculationMethod
import com.azkry.app.core.prayertimes.GeoLocation
import com.azkry.app.core.prayertimes.HighLatitudeRule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class PrayerSettings(
    val cityName: String = DEFAULT_CITY_NAME,
    val location: GeoLocation = DEFAULT_LOCATION,
    val method: CalculationMethod = CalculationMethod.UmmAlQura,
    val asrMadhab: AsrMadhab = AsrMadhab.Shafii,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.AngleBased,
    /** True once the user chose "current location": refreshed on app open. */
    val autoLocate: Boolean = false,
) {
    companion object {
        const val DEFAULT_CITY_NAME = "مكة المكرمة"
        val DEFAULT_LOCATION = GeoLocation(latitude = 21.4225, longitude = 39.8262)
    }
}

interface PrayerSettingsService {
    val settings: Flow<PrayerSettings>

    suspend fun setLocation(cityName: String, location: GeoLocation)
    suspend fun setMethod(method: CalculationMethod)
    suspend fun setAsrMadhab(asrMadhab: AsrMadhab)
    suspend fun setHighLatitudeRule(rule: HighLatitudeRule)
    suspend fun setAutoLocate(autoLocate: Boolean)
}

@Singleton
class DataStorePrayerSettingsService @Inject constructor(
    @ApplicationContext context: Context,
) : PrayerSettingsService {
    private val dataStore = context.prayerSettingsDataStore

    override val settings: Flow<PrayerSettings> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                val defaults = PrayerSettings()
                PrayerSettings(
                    cityName = preferences[Keys.CityName] ?: defaults.cityName,
                    location = GeoLocation(
                        latitude = preferences[Keys.Latitude] ?: defaults.location.latitude,
                        longitude = preferences[Keys.Longitude] ?: defaults.location.longitude,
                    ),
                    method = preferences[Keys.Method].toEnumOr(defaults.method),
                    asrMadhab = preferences[Keys.AsrMadhab].toEnumOr(defaults.asrMadhab),
                    highLatitudeRule = preferences[Keys.HighLatitudeRule].toEnumOr(defaults.highLatitudeRule),
                    autoLocate = preferences[Keys.AutoLocate] ?: false,
                )
            }

    override suspend fun setLocation(cityName: String, location: GeoLocation) {
        dataStore.edit { preferences ->
            preferences[Keys.CityName] = cityName
            preferences[Keys.Latitude] = location.latitude
            preferences[Keys.Longitude] = location.longitude
        }
    }

    override suspend fun setMethod(method: CalculationMethod) {
        dataStore.edit { it[Keys.Method] = method.name }
    }

    override suspend fun setAsrMadhab(asrMadhab: AsrMadhab) {
        dataStore.edit { it[Keys.AsrMadhab] = asrMadhab.name }
    }

    override suspend fun setHighLatitudeRule(rule: HighLatitudeRule) {
        dataStore.edit { it[Keys.HighLatitudeRule] = rule.name }
    }

    override suspend fun setAutoLocate(autoLocate: Boolean) {
        dataStore.edit { it[Keys.AutoLocate] = autoLocate }
    }

    private object Keys {
        val CityName = stringPreferencesKey("cityName")
        val Latitude = doublePreferencesKey("latitude")
        val Longitude = doublePreferencesKey("longitude")
        val Method = stringPreferencesKey("method")
        val AsrMadhab = stringPreferencesKey("asrMadhab")
        val HighLatitudeRule = stringPreferencesKey("highLatitudeRule")
        val AutoLocate = booleanPreferencesKey("autoLocate")
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

private val Context.prayerSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "prayer_settings",
)
