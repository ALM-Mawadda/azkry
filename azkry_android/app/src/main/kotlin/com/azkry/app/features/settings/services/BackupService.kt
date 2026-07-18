package com.azkry.app.features.settings.services

import com.azkry.app.core.database.AdhkarDao
import com.azkry.app.core.database.TrackingDao
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.models.PrayerLog
import com.azkry.app.core.models.WorshipLog
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BackupPrayerLog(val date: String, val prayer: String, val completed: Boolean)

@Serializable
private data class BackupWorshipLog(val date: String, val taskKey: String, val completed: Boolean)

@Serializable
private data class BackupDailyCount(val date: String, val dhikrId: Long, val count: Int)

@Serializable
private data class BackupFavorite(val dhikrId: Long, val createdAt: Long)

@Serializable
private data class BackupPayload(
    @SerialName("version") val version: Int = 1,
    val prayerLogs: List<BackupPrayerLog> = emptyList(),
    val worshipLogs: List<BackupWorshipLog> = emptyList(),
    val dailyCounts: List<BackupDailyCount> = emptyList(),
    val favorites: List<BackupFavorite> = emptyList(),
)

interface BackupService {
    suspend fun exportJson(): String

    /** Returns false when [json] is not a valid backup. Existing rows are upserted. */
    suspend fun importJson(json: String): Boolean
}

/**
 * Manual, user-initiated backup of the worship data as a JSON document. Seed
 * ids are deterministic (fixed insertion order), so dhikr references stay
 * valid across installs of the same app version.
 */
@Singleton
class JsonBackupService @Inject constructor(
    private val trackingDao: TrackingDao,
    private val adhkarDao: AdhkarDao,
) : BackupService {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun exportJson(): String {
        val payload = BackupPayload(
            prayerLogs = trackingDao.getAllPrayerLogs().map {
                BackupPrayerLog(it.date, it.prayer, it.completed)
            },
            worshipLogs = trackingDao.getAllWorshipLogs().map {
                BackupWorshipLog(it.date, it.taskKey, it.completed)
            },
            dailyCounts = adhkarDao.getAllDailyCounts().map {
                BackupDailyCount(it.date, it.dhikrId, it.count)
            },
            favorites = adhkarDao.getAllFavorites().map {
                BackupFavorite(it.dhikrId, it.createdAt)
            },
        )
        return json.encodeToString(payload)
    }

    override suspend fun importJson(jsonText: String): Boolean {
        val payload = runCatching {
            json.decodeFromString<BackupPayload>(jsonText)
        }.getOrNull() ?: return false

        trackingDao.upsertPrayerLogs(
            payload.prayerLogs.map { PrayerLog(it.date, it.prayer, it.completed) },
        )
        trackingDao.upsertWorshipLogs(
            payload.worshipLogs.map { WorshipLog(it.date, it.taskKey, it.completed) },
        )
        adhkarDao.upsertDailyCounts(
            payload.dailyCounts.map { DhikrDailyCount(it.date, it.dhikrId, it.count) },
        )
        payload.favorites.forEach { favorite ->
            adhkarDao.insertFavorite(FavoriteDhikr(favorite.dhikrId, favorite.createdAt))
        }
        return true
    }
}
