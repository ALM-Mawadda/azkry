package com.azkry.app.features.settings.services

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azkry.app.core.database.AzkryDatabase
import com.azkry.app.core.database.DatabaseReadiness
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.models.PrayerLog
import com.azkry.app.core.models.WorshipLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonBackupServiceTest {
    private val stores = mutableListOf<TestStore>()

    @After
    fun tearDown() {
        stores.forEach(TestStore::close)
    }

    @Test
    fun version2Backup_resolvesStableKeysAfterDhikrIdsChange() = runBlocking {
        val source = createStore(dhikrId = 17L)
        source.database.trackingDao().upsertPrayerLog(PrayerLog(DATE, "Fajr", true))
        source.database.trackingDao().upsertWorshipLog(WorshipLog(DATE, "QuranDaily", true))
        source.database.adhkarDao().upsertDailyCount(DhikrDailyCount(DATE, 17L, 3))
        source.database.adhkarDao().insertFavorite(FavoriteDhikr(17L, 1234L))

        val backup = source.service.exportJson()
        assertTrue(backup.contains("\"version\":2"))
        assertTrue(backup.contains("\"dhikrKey\":\"morning/item_1\""))

        val destination = createStore(dhikrId = 901L)
        assertTrue(destination.service.importJson(backup))

        assertEquals(
            listOf(DhikrDailyCount(DATE, 901L, 3)),
            destination.database.adhkarDao().getAllDailyCounts(),
        )
        assertEquals(
            listOf(FavoriteDhikr(901L, 1234L)),
            destination.database.adhkarDao().getAllFavorites(),
        )
        assertEquals(
            listOf(PrayerLog(DATE, "Fajr", true)),
            destination.database.trackingDao().getAllPrayerLogs(),
        )
        assertEquals(
            listOf(WorshipLog(DATE, "QuranDaily", true)),
            destination.database.trackingDao().getAllWorshipLogs(),
        )
    }

    @Test
    fun invalidPayloadsFailBeforeAnyWrites() = runBlocking {
        val store = createStore(dhikrId = 23L, repeatCount = 3)
        val invalidPayloads = listOf(
            """{"version":99}""",
            """{"version":2,"prayerLogs":[{"date":"19-07-2026","prayer":"Fajr","completed":true}]}""",
            """{"version":2,"prayerLogs":[{"date":"$DATE","prayer":"Sunrise","completed":true}]}""",
            """{"version":2,"worshipLogs":[{"date":"$DATE","taskKey":"unknown","completed":true}]}""",
            """{"version":2,"dailyCounts":[{"date":"$DATE","dhikrKey":"missing/item_1","count":1}]}""",
            """{"version":2,"dailyCounts":[{"date":"$DATE","dhikrKey":"morning/item_1","count":4}]}""",
            """{"version":2,"favorites":[{"dhikrKey":"morning/item_1","createdAt":-1}]}""",
            """{"version":2,"prayerLogs":[{"date":"$DATE","prayer":"Fajr","completed":true},{"date":"$DATE","prayer":"Fajr","completed":false}]}""",
            """{"version":2,"unexpected":true}""",
        )

        invalidPayloads.forEach { payload ->
            assertFalse(payload, store.service.importJson(payload))
            assertTrue(store.database.trackingDao().getAllPrayerLogs().isEmpty())
            assertTrue(store.database.trackingDao().getAllWorshipLogs().isEmpty())
            assertTrue(store.database.adhkarDao().getAllDailyCounts().isEmpty())
            assertTrue(store.database.adhkarDao().getAllFavorites().isEmpty())
        }
    }

    @Test
    fun failedWriteRollsBackEarlierTables() = runBlocking {
        val store = createStore(dhikrId = 29L)
        store.database.openHelper.writableDatabase.execSQL(
            """
            CREATE TRIGGER reject_backup_count
            BEFORE INSERT ON dhikr_daily_counts
            BEGIN
                SELECT RAISE(ABORT, 'forced failure');
            END
            """.trimIndent(),
        )
        val payload =
            """{"version":2,"prayerLogs":[{"date":"$DATE","prayer":"Fajr","completed":true}],"dailyCounts":[{"date":"$DATE","dhikrKey":"morning/item_1","count":1}]}"""

        assertFalse(store.service.importJson(payload))
        assertTrue(store.database.trackingDao().getAllPrayerLogs().isEmpty())
        assertTrue(store.database.adhkarDao().getAllDailyCounts().isEmpty())
    }

    @Test
    fun legacyBackupIsAcceptedOnlyWithoutRawDhikrReferences() = runBlocking {
        val store = createStore(dhikrId = 31L)
        val safeLegacy =
            """{"prayerLogs":[{"date":"$DATE","prayer":"Fajr","completed":true}]}"""
        val unsafeLegacy =
            """{"version":1,"dailyCounts":[{"date":"$DATE","dhikrId":31,"count":1}]}"""

        assertTrue(store.service.importJson(safeLegacy))
        assertFalse(store.service.importJson(unsafeLegacy))
        assertEquals(
            listOf(PrayerLog(DATE, "Fajr", true)),
            store.database.trackingDao().getAllPrayerLogs(),
        )
        assertTrue(store.database.adhkarDao().getAllDailyCounts().isEmpty())
    }

    private suspend fun createStore(
        dhikrId: Long,
        repeatCount: Int = 5,
    ): TestStore {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, AzkryDatabase::class.java).build()
        val categoryId = database.adhkarDao().insertCategory(
            DhikrCategory(
                key = "morning",
                title = "أذكار الصباح",
                iconKey = "sun",
                sortOrder = 0,
            ),
        )
        database.adhkarDao().insertAdhkar(
            listOf(
                Dhikr(
                    id = dhikrId,
                    categoryId = categoryId,
                    text = "سبحان الله",
                    repeatCount = repeatCount,
                    source = null,
                    sortOrder = 0,
                    stableKey = "morning/item_1",
                ),
            ),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val readiness = DatabaseReadiness(scope)
        readiness.initialize { }
        readiness.awaitReady()
        return TestStore(
            database = database,
            scope = scope,
            service = JsonBackupService(
                database = database,
                readiness = readiness,
                trackingDao = database.trackingDao(),
                adhkarDao = database.adhkarDao(),
            ),
        ).also(stores::add)
    }

    private data class TestStore(
        val database: AzkryDatabase,
        val scope: CoroutineScope,
        val service: JsonBackupService,
    ) {
        fun close() {
            database.close()
            scope.cancel()
        }
    }

    private companion object {
        const val DATE = "2026-07-19"
    }
}
