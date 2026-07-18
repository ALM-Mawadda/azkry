package com.azkry.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.models.SeedInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkarDao {
    @Query("SELECT * FROM dhikr_categories ORDER BY sortOrder")
    fun observeCategories(): Flow<List<DhikrCategory>>

    @Query("SELECT * FROM dhikr_categories WHERE key = :key")
    suspend fun categoryByKey(key: String): DhikrCategory?

    @Query("SELECT * FROM dhikr_categories WHERE id = :id")
    suspend fun categoryById(id: Long): DhikrCategory?

    @Query("SELECT * FROM adhkar WHERE categoryId = :categoryId ORDER BY sortOrder")
    fun observeAdhkar(categoryId: Long): Flow<List<Dhikr>>

    @Query("SELECT * FROM dhikr_daily_counts WHERE date = :date AND dhikrId IN (SELECT id FROM adhkar WHERE categoryId = :categoryId)")
    fun observeDailyCounts(categoryId: Long, date: String): Flow<List<DhikrDailyCount>>

    @Query("SELECT count FROM dhikr_daily_counts WHERE date = :date AND dhikrId = :dhikrId")
    suspend fun dailyCount(dhikrId: Long, date: String): Int?

    @Upsert
    suspend fun upsertDailyCount(count: DhikrDailyCount)

    @Upsert
    suspend fun upsertDailyCounts(counts: List<DhikrDailyCount>)

    @Query("DELETE FROM dhikr_daily_counts WHERE date = :date AND dhikrId = :dhikrId")
    suspend fun resetDailyCount(dhikrId: Long, date: String)

    @Query(
        """
        SELECT COUNT(*) FROM adhkar a
        JOIN dhikr_daily_counts c ON c.dhikrId = a.id AND c.date = :date
        WHERE a.categoryId = :categoryId AND c.count >= a.repeatCount
        """,
    )
    fun observeCompletedCount(categoryId: Long, date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM adhkar WHERE categoryId = :categoryId")
    fun observeTotalCount(categoryId: Long): Flow<Int>

    /**
     * Per-date count of categories (restricted to [keys]) whose adhkar are all
     * complete on that date. Dates with no complete category are absent.
     */
    @Query(
        """
        SELECT d.date AS date, COUNT(*) AS completedCategories FROM (
            SELECT dc.date AS date, a.categoryId AS categoryId
            FROM adhkar a
            JOIN dhikr_daily_counts dc ON dc.dhikrId = a.id
            JOIN dhikr_categories c ON c.id = a.categoryId
            WHERE dc.date BETWEEN :startDate AND :endDate AND c.`key` IN (:keys)
            GROUP BY dc.date, a.categoryId
            HAVING COUNT(CASE WHEN dc.count >= a.repeatCount THEN 1 END) = (
                SELECT COUNT(*) FROM adhkar WHERE categoryId = a.categoryId
            )
        ) d GROUP BY d.date
        """,
    )
    fun observeCompletedCategoriesByDate(
        startDate: String,
        endDate: String,
        keys: List<String>,
    ): Flow<List<DateCompletedCategories>>

    @Query("SELECT * FROM dhikr_daily_counts WHERE date = :date")
    fun observeAllDailyCounts(date: String): Flow<List<DhikrDailyCount>>

    @Query(
        """
        SELECT a.* FROM adhkar a
        JOIN favorite_adhkar f ON f.dhikrId = a.id
        ORDER BY f.createdAt DESC
        """,
    )
    fun observeFavoriteAdhkar(): Flow<List<Dhikr>>

    @Query("SELECT dhikrId FROM favorite_adhkar")
    fun observeFavoriteIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteDhikr)

    @Query("DELETE FROM favorite_adhkar WHERE dhikrId = :dhikrId")
    suspend fun deleteFavorite(dhikrId: Long)

    @Query("SELECT * FROM adhkar ORDER BY categoryId, sortOrder")
    suspend fun getAllAdhkar(): List<Dhikr>

    @Query("SELECT * FROM dhikr_daily_counts")
    suspend fun getAllDailyCounts(): List<DhikrDailyCount>

    @Query("SELECT * FROM favorite_adhkar")
    suspend fun getAllFavorites(): List<FavoriteDhikr>

    @Query("SELECT COUNT(*) FROM dhikr_categories")
    suspend fun categoryCount(): Int

    @Insert
    suspend fun insertCategory(category: DhikrCategory): Long

    @Insert
    suspend fun insertAdhkar(adhkar: List<Dhikr>)

    @Query("SELECT revision FROM seed_info WHERE id = 0")
    suspend fun seedRevision(): Int?

    @Upsert
    suspend fun setSeedInfo(info: SeedInfo)

    @Query("DELETE FROM dhikr_categories")
    suspend fun clearAllCategories()

    /**
     * Atomically replaces the whole seeded library. Deleting categories
     * cascades through adhkar to favorites and daily counts, so a content
     * upgrade starts those clean. Each item's categoryId is rewritten to the
     * freshly inserted category.
     */
    @Transaction
    suspend fun replaceSeededContent(
        categoriesWithItems: List<Pair<DhikrCategory, List<Dhikr>>>,
        revision: Int,
    ) {
        clearAllCategories()
        categoriesWithItems.forEach { (category, items) ->
            val categoryId = insertCategory(category)
            insertAdhkar(items.map { it.copy(categoryId = categoryId) })
        }
        setSeedInfo(SeedInfo(revision = revision))
    }
}

data class DateCompletedCategories(
    val date: String,
    val completedCategories: Int,
)
