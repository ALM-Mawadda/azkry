package com.azkry.app.features.tracking.services

import com.azkry.app.core.database.AdhkarDao
import com.azkry.app.core.database.TrackingDao
import com.azkry.app.core.models.DhikrCategoryKeys
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.PrayerLog
import com.azkry.app.core.models.WorshipLog
import com.azkry.app.core.models.WorshipTask
import com.azkry.app.features.tracking.models.WorshipScoring
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** Per-category adhkar progress shown in the tracking screen's adhkar rows. */
data class AdhkarCategoryProgress(
    val categoryId: Long,
    val categoryKey: String,
    val title: String,
    val completedCount: Int,
    val totalCount: Int,
) {
    val isCompleted: Boolean get() = totalCount > 0 && completedCount >= totalCount
}

data class DayTracking(
    val completedPrayers: Set<Prayer>,
    val completedTasks: Set<WorshipTask>,
    val adhkarProgress: List<AdhkarCategoryProgress>,
) {
    val percent: Int
        get() = WorshipScoring.dayPercent(
            completedPrayers = completedPrayers.size,
            completedAdhkarCategories = adhkarProgress.count { it.isCompleted },
            trackedAdhkarCategories = adhkarProgress.size,
            completedTasks = completedTasks,
        )
}

interface WorshipTrackingService {
    fun observeDay(date: String): Flow<DayTracking>

    /** [dates] must be non-empty local ISO dates; the result covers each of them. */
    fun observeWeekPercents(dates: List<String>): Flow<Map<String, Int>>

    suspend fun setPrayerCompleted(date: String, prayer: Prayer, completed: Boolean)
    suspend fun setTaskCompleted(date: String, task: WorshipTask, completed: Boolean)
}

/** The adhkar categories the tracking screen follows, in display order. */
private val TRACKED_CATEGORY_KEYS = listOf(
    DhikrCategoryKeys.MORNING,
    DhikrCategoryKeys.EVENING,
    DhikrCategoryKeys.SLEEP,
)

@Singleton
class RoomWorshipTrackingService @Inject constructor(
    private val trackingDao: TrackingDao,
    private val adhkarDao: AdhkarDao,
) : WorshipTrackingService {
    override fun observeDay(date: String): Flow<DayTracking> =
        combine(
            trackingDao.observePrayerLogs(date),
            trackingDao.observeWorshipLogs(date),
            observeAdhkarProgress(date),
        ) { prayerLogs, worshipLogs, adhkarProgress ->
            DayTracking(
                completedPrayers = prayerLogs.toCompletedPrayers(),
                completedTasks = worshipLogs.toCompletedTasks(),
                adhkarProgress = adhkarProgress,
            )
        }

    override fun observeWeekPercents(dates: List<String>): Flow<Map<String, Int>> {
        val startDate = dates.min()
        val endDate = dates.max()
        return combine(
            trackingDao.observePrayerLogsBetween(startDate, endDate),
            trackingDao.observeWorshipLogsBetween(startDate, endDate),
            adhkarDao.observeCompletedCategoriesByDate(startDate, endDate, TRACKED_CATEGORY_KEYS),
        ) { prayerLogs, worshipLogs, adhkarCompleted ->
            val completedByDate = adhkarCompleted.associate { it.date to it.completedCategories }
            dates.associateWith { date ->
                WorshipScoring.dayPercent(
                    completedPrayers = prayerLogs.filter { it.date == date }.toCompletedPrayers().size,
                    completedAdhkarCategories = completedByDate[date] ?: 0,
                    trackedAdhkarCategories = TRACKED_CATEGORY_KEYS.size,
                    completedTasks = worshipLogs.filter { it.date == date }.toCompletedTasks(),
                )
            }
        }
    }

    override suspend fun setPrayerCompleted(date: String, prayer: Prayer, completed: Boolean) {
        trackingDao.upsertPrayerLog(
            PrayerLog(date = date, prayer = prayer.name, completed = completed),
        )
    }

    override suspend fun setTaskCompleted(date: String, task: WorshipTask, completed: Boolean) {
        trackingDao.upsertWorshipLog(
            WorshipLog(date = date, taskKey = task.key, completed = completed),
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAdhkarProgress(date: String): Flow<List<AdhkarCategoryProgress>> =
        adhkarDao.observeCategories().flatMapLatest { categories ->
            val tracked = categories
                .filter { it.key in TRACKED_CATEGORY_KEYS }
                .sortedBy { TRACKED_CATEGORY_KEYS.indexOf(it.key) }
            if (tracked.isEmpty()) return@flatMapLatest flowOf(emptyList())
            val perCategory = tracked.map { category ->
                combine(
                    adhkarDao.observeCompletedCount(category.id, date),
                    adhkarDao.observeTotalCount(category.id),
                ) { completed, total ->
                    AdhkarCategoryProgress(
                        categoryId = category.id,
                        categoryKey = category.key,
                        title = category.title,
                        completedCount = completed,
                        totalCount = total,
                    )
                }
            }
            combine(perCategory) { it.toList() }
        }
}

private fun List<PrayerLog>.toCompletedPrayers(): Set<Prayer> =
    filter { it.completed }
        .mapNotNull { log -> Prayer.entries.firstOrNull { it.name == log.prayer } }
        .toSet()

private fun List<WorshipLog>.toCompletedTasks(): Set<WorshipTask> =
    filter { it.completed }
        .mapNotNull { WorshipTask.fromKey(it.taskKey) }
        .toSet()
