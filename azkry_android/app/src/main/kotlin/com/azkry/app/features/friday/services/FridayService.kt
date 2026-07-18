package com.azkry.app.features.friday.services

import com.azkry.app.core.database.TrackingDao
import com.azkry.app.core.models.WorshipLog
import com.azkry.app.features.friday.models.FridaySunnah
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FridayService {
    fun observeChecks(date: String): Flow<Set<FridaySunnah>>
    suspend fun setChecked(date: String, sunnah: FridaySunnah, checked: Boolean)
}

@Singleton
class RoomFridayService @Inject constructor(
    private val trackingDao: TrackingDao,
) : FridayService {
    override fun observeChecks(date: String): Flow<Set<FridaySunnah>> =
        trackingDao.observeWorshipLogs(date).map { logs ->
            val completedKeys = logs.filter { it.completed }.map { it.taskKey }.toSet()
            FridaySunnah.entries.filter { it.taskKey in completedKeys }.toSet()
        }

    override suspend fun setChecked(date: String, sunnah: FridaySunnah, checked: Boolean) {
        trackingDao.upsertWorshipLog(
            WorshipLog(date = date, taskKey = sunnah.taskKey, completed = checked),
        )
    }
}
