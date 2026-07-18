package com.azkry.app.features.adhkar.services

import com.azkry.app.core.database.AdhkarDao
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.utilities.containsArabic
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AdhkarService {
    fun observeCategories(): Flow<List<DhikrCategory>>
    suspend fun categoryById(id: Long): DhikrCategory?
    fun observeAdhkar(categoryId: Long): Flow<List<Dhikr>>
    fun observeDailyCounts(categoryId: Long, date: String): Flow<List<DhikrDailyCount>>

    /** Increments the day counter of [dhikr], capped at its repeat count. */
    suspend fun incrementCount(dhikr: Dhikr, date: String)
    suspend fun resetCount(dhikrId: Long, date: String)

    /** Marks every dhikr in [adhkar] fully done for [date] (the "انتهيت؟" button). */
    suspend fun completeAll(adhkar: List<Dhikr>, date: String)

    fun observeFavorites(): Flow<List<Dhikr>>
    fun observeFavoriteIds(): Flow<Set<Long>>
    fun observeAllDailyCounts(date: String): Flow<List<DhikrDailyCount>>
    suspend fun setFavorite(dhikrId: Long, favorite: Boolean)

    suspend fun categoryByKey(key: String): DhikrCategory?
    suspend fun search(query: String): List<Dhikr>
}

@Singleton
class RoomAdhkarService @Inject constructor(
    private val adhkarDao: AdhkarDao,
) : AdhkarService {
    override fun observeCategories(): Flow<List<DhikrCategory>> = adhkarDao.observeCategories()

    override suspend fun categoryById(id: Long): DhikrCategory? = adhkarDao.categoryById(id)

    override fun observeAdhkar(categoryId: Long): Flow<List<Dhikr>> =
        adhkarDao.observeAdhkar(categoryId)

    override fun observeDailyCounts(categoryId: Long, date: String): Flow<List<DhikrDailyCount>> =
        adhkarDao.observeDailyCounts(categoryId, date)

    override suspend fun incrementCount(dhikr: Dhikr, date: String) {
        val current = adhkarDao.dailyCount(dhikr.id, date) ?: 0
        if (current >= dhikr.repeatCount) return
        adhkarDao.upsertDailyCount(
            DhikrDailyCount(date = date, dhikrId = dhikr.id, count = current + 1),
        )
    }

    override suspend fun resetCount(dhikrId: Long, date: String) {
        adhkarDao.resetDailyCount(dhikrId, date)
    }

    override suspend fun completeAll(adhkar: List<Dhikr>, date: String) {
        adhkarDao.upsertDailyCounts(
            adhkar.map { dhikr ->
                DhikrDailyCount(date = date, dhikrId = dhikr.id, count = dhikr.repeatCount)
            },
        )
    }

    override fun observeFavorites(): Flow<List<Dhikr>> = adhkarDao.observeFavoriteAdhkar()

    override fun observeFavoriteIds(): Flow<Set<Long>> =
        adhkarDao.observeFavoriteIds().map { it.toSet() }

    override fun observeAllDailyCounts(date: String): Flow<List<DhikrDailyCount>> =
        adhkarDao.observeAllDailyCounts(date)

    override suspend fun setFavorite(dhikrId: Long, favorite: Boolean) {
        if (favorite) {
            adhkarDao.insertFavorite(
                FavoriteDhikr(dhikrId = dhikrId, createdAt = Instant.now().toEpochMilli()),
            )
        } else {
            adhkarDao.deleteFavorite(dhikrId)
        }
    }

    override suspend fun categoryByKey(key: String): DhikrCategory? = adhkarDao.categoryByKey(key)

    override suspend fun search(query: String): List<Dhikr> {
        if (query.isBlank()) return emptyList()
        // The corpus is small; normalized in-memory matching lets plain
        // queries hit fully vocalized texts.
        return adhkarDao.getAllAdhkar()
            .filter { dhikr ->
                dhikr.text.containsArabic(query) ||
                    dhikr.title?.containsArabic(query) == true
            }
            .take(50)
    }
}
