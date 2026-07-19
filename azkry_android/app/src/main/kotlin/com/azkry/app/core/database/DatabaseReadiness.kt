package com.azkry.app.core.database

import com.azkry.app.core.coroutines.ApplicationScope
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.models.SeedInfo
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * Process-owned completion signal for database migration and content seeding.
 * Consumers either observe a fully seeded database or receive the initializer
 * failure; they never race an unowned first-open coroutine.
 */
@Singleton
class DatabaseReadiness @Inject constructor(
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val started = AtomicBoolean(false)
    private val completion = CompletableDeferred<Unit>()

    fun initialize(initializer: suspend () -> Unit) {
        check(started.compareAndSet(false, true)) { "Database initialization already started" }
        val job = applicationScope.launch(Dispatchers.IO) {
            try {
                initializer()
                completion.complete(Unit)
            } catch (error: Throwable) {
                completion.completeExceptionally(error)
                if (error is CancellationException) throw error
            }
        }
        job.invokeOnCompletion { error ->
            if (error != null) completion.completeExceptionally(error)
        }
    }

    suspend fun awaitReady() {
        completion.await()
    }

    internal fun <T> afterReady(upstream: () -> Flow<T>): Flow<T> = flow {
        awaitReady()
        emitAll(upstream())
    }
}

/** Ensures every existing AdhkarDao consumer shares the same readiness gate. */
internal class ReadyAdhkarDao(
    private val delegate: AdhkarDao,
    private val readiness: DatabaseReadiness,
) : AdhkarDao {
    override fun observeCategories(): Flow<List<DhikrCategory>> =
        readiness.afterReady(delegate::observeCategories)

    override suspend fun categoryByKey(key: String): DhikrCategory? = ready {
        delegate.categoryByKey(key)
    }

    override suspend fun categoryById(id: Long): DhikrCategory? = ready {
        delegate.categoryById(id)
    }

    override fun observeAdhkar(categoryId: Long): Flow<List<Dhikr>> =
        readiness.afterReady { delegate.observeAdhkar(categoryId) }

    override fun observeDailyCounts(
        categoryId: Long,
        date: String,
    ): Flow<List<DhikrDailyCount>> = readiness.afterReady {
        delegate.observeDailyCounts(categoryId, date)
    }

    override suspend fun dailyCount(dhikrId: Long, date: String): Int? = ready {
        delegate.dailyCount(dhikrId, date)
    }

    override suspend fun upsertDailyCount(count: DhikrDailyCount) = ready {
        delegate.upsertDailyCount(count)
    }

    override suspend fun upsertDailyCounts(counts: List<DhikrDailyCount>) = ready {
        delegate.upsertDailyCounts(counts)
    }

    override suspend fun incrementDailyCount(dhikrId: Long, date: String) = ready {
        delegate.incrementDailyCount(dhikrId, date)
    }

    override suspend fun resetDailyCount(dhikrId: Long, date: String) = ready {
        delegate.resetDailyCount(dhikrId, date)
    }

    override fun observeCompletedCount(categoryId: Long, date: String): Flow<Int> =
        readiness.afterReady { delegate.observeCompletedCount(categoryId, date) }

    override fun observeTotalCount(categoryId: Long): Flow<Int> =
        readiness.afterReady { delegate.observeTotalCount(categoryId) }

    override fun observeCompletedCategoriesByDate(
        startDate: String,
        endDate: String,
        keys: List<String>,
    ): Flow<List<DateCompletedCategories>> = readiness.afterReady {
        delegate.observeCompletedCategoriesByDate(startDate, endDate, keys)
    }

    override fun observeAllDailyCounts(date: String): Flow<List<DhikrDailyCount>> =
        readiness.afterReady { delegate.observeAllDailyCounts(date) }

    override fun observeFavoriteAdhkar(): Flow<List<Dhikr>> =
        readiness.afterReady(delegate::observeFavoriteAdhkar)

    override fun observeFavoriteIds(): Flow<List<Long>> =
        readiness.afterReady(delegate::observeFavoriteIds)

    override suspend fun insertFavorite(favorite: FavoriteDhikr) = ready {
        delegate.insertFavorite(favorite)
    }

    override suspend fun upsertFavorites(favorites: List<FavoriteDhikr>) = ready {
        delegate.upsertFavorites(favorites)
    }

    override suspend fun deleteFavorite(dhikrId: Long) = ready {
        delegate.deleteFavorite(dhikrId)
    }

    override suspend fun getAllAdhkar(): List<Dhikr> = ready(delegate::getAllAdhkar)

    override suspend fun getAllDailyCounts(): List<DhikrDailyCount> =
        ready(delegate::getAllDailyCounts)

    override suspend fun getAllFavorites(): List<FavoriteDhikr> =
        ready(delegate::getAllFavorites)

    override suspend fun categoryCount(): Int = ready(delegate::categoryCount)

    override suspend fun insertCategory(category: DhikrCategory): Long = ready {
        delegate.insertCategory(category)
    }

    override suspend fun insertAdhkar(adhkar: List<Dhikr>) = ready {
        delegate.insertAdhkar(adhkar)
    }

    override suspend fun seedRevision(): Int? = ready(delegate::seedRevision)

    override suspend fun setSeedInfo(info: SeedInfo) = ready {
        delegate.setSeedInfo(info)
    }

    override suspend fun clearAllCategories() = ready(delegate::clearAllCategories)

    override suspend fun clearAllDailyCounts() = ready(delegate::clearAllDailyCounts)

    override suspend fun replaceSeededContent(
        categoriesWithItems: List<Pair<DhikrCategory, List<Dhikr>>>,
        revision: Int,
    ) = ready {
        delegate.replaceSeededContent(categoriesWithItems, revision)
    }

    private suspend fun <T> ready(block: suspend () -> T): T {
        readiness.awaitReady()
        return block()
    }
}
