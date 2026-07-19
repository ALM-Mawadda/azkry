package com.azkry.app.features.settings.services

import androidx.room.withTransaction
import com.azkry.app.core.database.AdhkarDao
import com.azkry.app.core.database.AzkryDatabase
import com.azkry.app.core.database.DatabaseReadiness
import com.azkry.app.core.database.TrackingDao
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.PrayerLog
import com.azkry.app.core.models.WorshipLog
import com.azkry.app.core.models.WorshipTask
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val LEGACY_BACKUP_VERSION = 1
private const val CURRENT_BACKUP_VERSION = 2

@Serializable
private data class BackupPrayerLog(val date: String, val prayer: String, val completed: Boolean)

@Serializable
private data class BackupWorshipLog(val date: String, val taskKey: String, val completed: Boolean)

@Serializable
private data class LegacyBackupDailyCount(val date: String, val dhikrId: Long, val count: Int)

@Serializable
private data class LegacyBackupFavorite(val dhikrId: Long, val createdAt: Long)

@Serializable
private data class LegacyBackupPayload(
    @SerialName("version") val version: Int = LEGACY_BACKUP_VERSION,
    val prayerLogs: List<BackupPrayerLog> = emptyList(),
    val worshipLogs: List<BackupWorshipLog> = emptyList(),
    val dailyCounts: List<LegacyBackupDailyCount> = emptyList(),
    val favorites: List<LegacyBackupFavorite> = emptyList(),
)

@Serializable
private data class StableBackupDailyCount(val date: String, val dhikrKey: String, val count: Int)

@Serializable
private data class StableBackupFavorite(val dhikrKey: String, val createdAt: Long)

@Serializable
private data class BackupPayloadV2(
    @SerialName("version") val version: Int = CURRENT_BACKUP_VERSION,
    val prayerLogs: List<BackupPrayerLog> = emptyList(),
    val worshipLogs: List<BackupWorshipLog> = emptyList(),
    val dailyCounts: List<StableBackupDailyCount> = emptyList(),
    val favorites: List<StableBackupFavorite> = emptyList(),
)

private sealed interface DecodedBackup {
    data class Legacy(val payload: LegacyBackupPayload) : DecodedBackup
    data class Stable(val payload: BackupPayloadV2) : DecodedBackup
}

private data class ValidatedBackup(
    val prayerLogs: List<PrayerLog>,
    val worshipLogs: List<WorshipLog>,
    val dailyCounts: List<DhikrDailyCount>,
    val favorites: List<FavoriteDhikr>,
)

interface BackupService {
    suspend fun exportJson(): String

    /** Returns false when [json] fails full validation or an atomic import. */
    suspend fun importJson(json: String): Boolean
}

/** Manual, user-initiated backup of all worship data as versioned JSON. */
@Singleton
class JsonBackupService @Inject constructor(
    private val database: AzkryDatabase,
    private val readiness: DatabaseReadiness,
    private val trackingDao: TrackingDao,
    private val adhkarDao: AdhkarDao,
) : BackupService {
    private val codec = Json { encodeDefaults = true }

    override suspend fun exportJson(): String {
        readiness.awaitReady()
        val payload = database.withTransaction {
            val adhkar = adhkarDao.getAllAdhkar()
            val adhkarById = adhkar.associateBy(Dhikr::id)
            check(adhkarById.size == adhkar.size) { "Duplicate dhikr row ids" }
            check(adhkar.all { it.stableKey.isNotBlank() }) { "Dhikr stable key is missing" }
            check(adhkar.map(Dhikr::stableKey).distinct().size == adhkar.size) {
                "Duplicate dhikr stable keys"
            }

            val snapshot = BackupPayloadV2(
                prayerLogs = trackingDao.getAllPrayerLogs()
                    .map { BackupPrayerLog(it.date, it.prayer, it.completed) }
                    .sortedWith(compareBy(BackupPrayerLog::date, BackupPrayerLog::prayer)),
                worshipLogs = trackingDao.getAllWorshipLogs()
                    .map { BackupWorshipLog(it.date, it.taskKey, it.completed) }
                    .sortedWith(compareBy(BackupWorshipLog::date, BackupWorshipLog::taskKey)),
                dailyCounts = adhkarDao.getAllDailyCounts()
                    .map { count ->
                        val dhikr = checkNotNull(adhkarById[count.dhikrId]) {
                            "Daily count references missing dhikr ${count.dhikrId}"
                        }
                        StableBackupDailyCount(count.date, dhikr.stableKey, count.count)
                    }
                    .sortedWith(compareBy(StableBackupDailyCount::date, StableBackupDailyCount::dhikrKey)),
                favorites = adhkarDao.getAllFavorites()
                    .map { favorite ->
                        val dhikr = checkNotNull(adhkarById[favorite.dhikrId]) {
                            "Favorite references missing dhikr ${favorite.dhikrId}"
                        }
                        StableBackupFavorite(dhikr.stableKey, favorite.createdAt)
                    }
                    .sortedBy(StableBackupFavorite::dhikrKey),
            )
            check(snapshot.validate(adhkar.associateBy(Dhikr::stableKey)) != null) {
                "Database contains data that cannot be backed up safely"
            }
            snapshot
        }
        return codec.encodeToString(payload)
    }

    override suspend fun importJson(json: String): Boolean {
        val decoded = codec.decodeBackup(json) ?: return false
        readiness.awaitReady()
        return try {
            database.withTransaction {
                val validated = when (decoded) {
                    is DecodedBackup.Legacy -> decoded.payload.validate()
                    is DecodedBackup.Stable -> {
                        val adhkar = adhkarDao.getAllAdhkar()
                        val byStableKey = adhkar.associateBy(Dhikr::stableKey)
                        if (byStableKey.size != adhkar.size) null else decoded.payload.validate(byStableKey)
                    }
                } ?: return@withTransaction false

                // Validation above is complete. No write is attempted before
                // every row and every dhikr reference is known to be valid.
                trackingDao.upsertPrayerLogs(validated.prayerLogs)
                trackingDao.upsertWorshipLogs(validated.worshipLogs)
                adhkarDao.upsertDailyCounts(validated.dailyCounts)
                adhkarDao.upsertFavorites(validated.favorites)
                true
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            false
        }
    }
}

private fun Json.decodeBackup(jsonText: String): DecodedBackup? = runCatching {
    val root = parseToJsonElement(jsonText).jsonObject
    val version = root["version"]?.jsonPrimitive?.intOrNull ?: LEGACY_BACKUP_VERSION
    when (version) {
        LEGACY_BACKUP_VERSION -> DecodedBackup.Legacy(
            decodeFromJsonElement<LegacyBackupPayload>(root),
        )
        CURRENT_BACKUP_VERSION -> DecodedBackup.Stable(
            decodeFromJsonElement<BackupPayloadV2>(root),
        )
        else -> null
    }
}.getOrNull()

/**
 * V1 dhikr references are raw row ids with no source seed revision. They can
 * silently resolve to different content after reseeding, so only the
 * reference-free tracking subset is safe to preserve.
 */
private fun LegacyBackupPayload.validate(): ValidatedBackup? {
    if (version != LEGACY_BACKUP_VERSION || dailyCounts.isNotEmpty() || favorites.isNotEmpty()) {
        return null
    }
    val common = validateCommon(prayerLogs, worshipLogs) ?: return null
    return ValidatedBackup(common.first, common.second, emptyList(), emptyList())
}

private fun BackupPayloadV2.validate(adhkarByStableKey: Map<String, Dhikr>): ValidatedBackup? {
    if (version != CURRENT_BACKUP_VERSION || adhkarByStableKey.keys.any(String::isBlank)) return null
    val common = validateCommon(prayerLogs, worshipLogs) ?: return null
    if (!dailyCounts.haveUniqueKeys { it.date to it.dhikrKey }) return null
    if (!favorites.haveUniqueKeys(StableBackupFavorite::dhikrKey)) return null

    val validatedCounts = ArrayList<DhikrDailyCount>(dailyCounts.size)
    dailyCounts.forEach { count ->
        if (!count.date.isLocalIsoDate() || count.dhikrKey.isBlank()) return null
        val dhikr = adhkarByStableKey[count.dhikrKey] ?: return null
        if (count.count !in 0..dhikr.repeatCount) return null
        validatedCounts += DhikrDailyCount(count.date, dhikr.id, count.count)
    }

    val validatedFavorites = ArrayList<FavoriteDhikr>(favorites.size)
    favorites.forEach { favorite ->
        if (favorite.dhikrKey.isBlank() || favorite.createdAt < 0L) return null
        val dhikr = adhkarByStableKey[favorite.dhikrKey] ?: return null
        validatedFavorites += FavoriteDhikr(dhikr.id, favorite.createdAt)
    }
    return ValidatedBackup(common.first, common.second, validatedCounts, validatedFavorites)
}

private fun validateCommon(
    prayerLogs: List<BackupPrayerLog>,
    worshipLogs: List<BackupWorshipLog>,
): Pair<List<PrayerLog>, List<WorshipLog>>? {
    if (!prayerLogs.haveUniqueKeys { it.date to it.prayer }) return null
    if (!worshipLogs.haveUniqueKeys { it.date to it.taskKey }) return null

    val prayers = prayerLogs.map { log ->
        if (!log.date.isLocalIsoDate() || Prayer.obligatory.none { it.name == log.prayer }) {
            return null
        }
        PrayerLog(log.date, log.prayer, log.completed)
    }
    val worship = worshipLogs.map { log ->
        if (!log.date.isLocalIsoDate() || WorshipTask.fromKey(log.taskKey) == null) return null
        WorshipLog(log.date, log.taskKey, log.completed)
    }
    return prayers to worship
}

private fun String.isLocalIsoDate(): Boolean =
    runCatching { LocalDate.parse(this) }.getOrNull()?.toString() == this

private inline fun <T, K> List<T>.haveUniqueKeys(keyOf: (T) -> K): Boolean {
    val seen = HashSet<K>(size)
    return all { seen.add(keyOf(it)) }
}
