package com.azkry.app.features.mushaf.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azkry.app.features.mushaf.models.Khatmah
import com.azkry.app.features.mushaf.models.LastRead
import com.azkry.app.features.mushaf.models.QuranBookmark
import com.azkry.app.features.mushaf.models.QuranIndex
import com.azkry.app.features.mushaf.models.SurahContent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface QuranService {
    suspend fun index(): QuranIndex
    suspend fun surah(number: Int): SurahContent

    val lastRead: Flow<LastRead?>
    suspend fun saveLastRead(lastRead: LastRead)

    val bookmarks: Flow<List<QuranBookmark>>
    suspend fun toggleBookmark(bookmark: QuranBookmark)

    val khatmah: Flow<Khatmah?>
    suspend fun startKhatmah(totalDays: Int, startDateKey: String)
    suspend fun finishKhatmah()
}

/**
 * Reads the bundled Quran text (public Tanzil Uthmani text via the
 * alquran.cloud dataset) from per-surah asset files. The index is cached for
 * the process lifetime; surah content is parsed on demand so no full-Quran
 * blob ever sits in memory.
 */
@Singleton
class AssetQuranService @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : QuranService {
    private val json = Json { ignoreUnknownKeys = true }
    private val indexMutex = Mutex()
    private var cachedIndex: QuranIndex? = null
    private val dataStore = context.quranDataStore

    override suspend fun index(): QuranIndex = indexMutex.withLock {
        cachedIndex ?: withContext(Dispatchers.IO) {
            json.decodeFromString<QuranIndex>(readAsset("quran/index.json"))
        }.also { cachedIndex = it }
    }

    override suspend fun surah(number: Int): SurahContent {
        require(number in 1..114) { "surah number out of range: $number" }
        return withContext(Dispatchers.IO) {
            json.decodeFromString<SurahContent>(readAsset("quran/surah_$number.json"))
        }
    }

    override val lastRead: Flow<LastRead?> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                val surahNumber = preferences[Keys.Surah] ?: return@map null
                LastRead(
                    surahNumber = surahNumber,
                    surahName = preferences[Keys.SurahName].orEmpty(),
                    ayahNumber = preferences[Keys.Ayah] ?: 1,
                    page = preferences[Keys.Page] ?: 1,
                    timestampMillis = preferences[Keys.Timestamp] ?: 0L,
                )
            }

    override suspend fun saveLastRead(lastRead: LastRead) {
        dataStore.edit { preferences ->
            preferences[Keys.Surah] = lastRead.surahNumber
            preferences[Keys.SurahName] = lastRead.surahName
            preferences[Keys.Ayah] = lastRead.ayahNumber
            preferences[Keys.Page] = lastRead.page
            preferences[Keys.Timestamp] = lastRead.timestampMillis
        }
    }

    override val bookmarks: Flow<List<QuranBookmark>> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                preferences[Keys.Bookmarks]?.let { raw ->
                    runCatching { json.decodeFromString<List<QuranBookmark>>(raw) }.getOrNull()
                }.orEmpty()
            }

    override suspend fun toggleBookmark(bookmark: QuranBookmark) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.Bookmarks]?.let { raw ->
                runCatching { json.decodeFromString<List<QuranBookmark>>(raw) }.getOrNull()
            }.orEmpty()
            val updated = if (current.any { it.page == bookmark.page }) {
                current.filterNot { it.page == bookmark.page }
            } else {
                (current + bookmark).sortedBy { it.page }
            }
            preferences[Keys.Bookmarks] = json.encodeToString(updated)
        }
    }

    override val khatmah: Flow<Khatmah?> =
        dataStore.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .map { preferences ->
                val start = preferences[Keys.KhatmahStart] ?: return@map null
                val days = preferences[Keys.KhatmahDays] ?: return@map null
                Khatmah(startDateKey = start, totalDays = days)
            }

    override suspend fun startKhatmah(totalDays: Int, startDateKey: String) {
        dataStore.edit { preferences ->
            preferences[Keys.KhatmahStart] = startDateKey
            preferences[Keys.KhatmahDays] = totalDays
        }
    }

    override suspend fun finishKhatmah() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.KhatmahStart)
            preferences.remove(Keys.KhatmahDays)
        }
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    private object Keys {
        val Surah = intPreferencesKey("lastSurah")
        val SurahName = stringPreferencesKey("lastSurahName")
        val Ayah = intPreferencesKey("lastAyah")
        val Page = intPreferencesKey("lastPage")
        val Timestamp = longPreferencesKey("lastTimestamp")
        val Bookmarks = stringPreferencesKey("bookmarks")
        val KhatmahStart = stringPreferencesKey("khatmahStart")
        val KhatmahDays = intPreferencesKey("khatmahDays")
    }
}

private val Context.quranDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quran_prefs",
)
