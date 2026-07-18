package com.azkry.app.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.azkry.app.core.models.PrayerLog
import com.azkry.app.core.models.WorshipLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingDao {
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun observePrayerLogs(date: String): Flow<List<PrayerLog>>

    @Query("SELECT * FROM prayer_logs WHERE date BETWEEN :startDate AND :endDate")
    fun observePrayerLogsBetween(startDate: String, endDate: String): Flow<List<PrayerLog>>

    @Upsert
    suspend fun upsertPrayerLog(log: PrayerLog)

    @Query("SELECT * FROM worship_logs WHERE date = :date")
    fun observeWorshipLogs(date: String): Flow<List<WorshipLog>>

    @Query("SELECT * FROM worship_logs WHERE date BETWEEN :startDate AND :endDate")
    fun observeWorshipLogsBetween(startDate: String, endDate: String): Flow<List<WorshipLog>>

    @Upsert
    suspend fun upsertWorshipLog(log: WorshipLog)

    @Query("SELECT * FROM prayer_logs")
    suspend fun getAllPrayerLogs(): List<PrayerLog>

    @Query("SELECT * FROM worship_logs")
    suspend fun getAllWorshipLogs(): List<WorshipLog>

    @Upsert
    suspend fun upsertPrayerLogs(logs: List<PrayerLog>)

    @Upsert
    suspend fun upsertWorshipLogs(logs: List<WorshipLog>)
}
